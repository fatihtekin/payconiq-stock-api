package com.payconiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * A Swagger configuration for the Stock API
 */
@Configuration
@EnableSwagger2
public class StockSwaggerConfig {
    /**
     * @return the {@link Docket} for the Stock API
     */
    @Bean
    public Docket messagesApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("payconiq").apiInfo(
                new ApiInfoBuilder().title("STOCK API").description("REST API for Payconiq Stock").version("1.0").build()).
                select().build();
    }
}
