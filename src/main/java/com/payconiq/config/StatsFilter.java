package com.payconiq.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import io.prometheus.client.SimpleTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static com.payconiq.config.StockMetricsConfig.STOCK_API_HISTOGRAM;

/**
 * This filter catches all the requests and once they need to be converted to generalize
 * url pattern then converts them and then sends them to STOCK_API_HISTOGRAM histogram.
 * an example: /api/stock/23 => /api/stock/*
 *
 * If needed in the future new patterns can be added to GENERALIZE_PATH_MAP
 *
 * An example format in metrics is as
 * http_request_duration_seconds_bucket{method="POST",path="/api/stocks",status="201",le="0.5",} 1.0
 *
 * This code makes {@link com.payconiq.endpoint.StockController} simpler and keeps it more maintainable when adding new endpoints into stock-api
 *
 */
@Component
@WebFilter("/*")
public class StatsFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsFilter.class);

    private static final Map<Pattern, String> GENERALIZE_PATH_MAP = new HashMap<Pattern, String>() {{
        put(Pattern.compile("\\/api\\/stocks\\/.+"), "/api/stocks/*");
    }};

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            SimpleTimer timer = new SimpleTimer();
            try {
                chain.doFilter(req, resp);
            } finally {
                final double elapsedSeconds = timer.elapsedSeconds();
                final HttpServletRequest httpRequest = (HttpServletRequest) req;
                String statusCode = String.valueOf(((HttpServletResponse) resp).getStatus());
                String servletPath = httpRequest.getServletPath();
                Optional<Map.Entry<Pattern, String>> patternToPath = GENERALIZE_PATH_MAP.entrySet().stream()
                        .filter(e -> e.getKey().matcher(httpRequest.getServletPath()).matches())
                        .findFirst();
                if (patternToPath.isPresent()) {
                    servletPath = patternToPath.get().getValue();
                }
                STOCK_API_HISTOGRAM.labels(httpRequest.getMethod(), servletPath, statusCode).observe(elapsedSeconds);
                LOGGER.trace("{}: {} seconds ", httpRequest.getServletPath(), elapsedSeconds);
            }
            return;
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {}
}