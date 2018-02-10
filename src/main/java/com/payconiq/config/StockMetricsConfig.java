package com.payconiq.config;

import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Metrics configuration for Prometheus
 * Probably use more generic metrics implementation using something like https://micrometer.io/
 */
@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
public class StockMetricsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(StockMetricsConfig.class);

    private String urlPath;

    @Value("${prometheus.url.path:/metrics}")
    public void setUrlPath(final String urlPath) {
        this.urlPath = urlPath;
    }

    public static final double[] buckets = Stream.of(0.0001d, 0.001d, 0.01d, 0.1d, 1d, 10d)
            .map(n -> Arrays.asList(n*1, n*2, n*5))
            .flatMap(List::stream).mapToDouble(Double::doubleValue).toArray();

    public static final Histogram STOCK_API_REQUEST_HISTOGRAM = Histogram.build()
            .name("http_request_duration_seconds")
            .help("Duration of HTTP request in seconds")
            .labelNames("method", "path", "status")
            .buckets(buckets)
            .register();

    @Bean
    public SpringBootMetricsCollector springBootMetricsCollector(final Collection<PublicMetrics> publicMetrics) {
        LOG.info("Loading Prometheus Spring Boot metrics");
        final SpringBootMetricsCollector metricsCollector = new SpringBootMetricsCollector(publicMetrics);
        metricsCollector.register();
        return metricsCollector;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        LOG.info("Initialising included Collectors from Prometheus");
        DefaultExports.initialize();
        LOG.info("Creating Prometheus MetricsServlet on path: {}", urlPath);
        return new ServletRegistrationBean(new MetricsServlet(), urlPath);
    }

}
