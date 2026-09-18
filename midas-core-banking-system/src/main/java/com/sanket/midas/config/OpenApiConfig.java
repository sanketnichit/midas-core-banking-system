package com.sanket.midas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI midasOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Midas Core Banking API")
                                .version("1.0.0")
                                .description(
                                        "Event-driven banking backend with "
                                                + "PostgreSQL, Apache Kafka, "
                                                + "transaction processing, "
                                                + "incentives, and the Outbox pattern."
                                )
                                .contact(
                                        new Contact()
                                                .name("Sanket")
                                )
                );
    }
}