package com.payconiq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Runner class for the Stock API
 */
@SpringBootApplication
public class StockControllerApiApplication {
    private static final Logger LOG = LoggerFactory.getLogger(StockControllerApiApplication.class);

    /**
     * Start the {@link StockControllerApiApplication} with the given arguments
     * @param args the {@link String} array of arguments to pass to the {@link SpringApplicationBuilder}
     */
    public static void main(final String[] args) {
        LOG.info("Stock API Application starting");
        new SpringApplicationBuilder(StockControllerApiApplication.class).bannerMode(Banner.Mode.OFF).run(args);
    }
}
