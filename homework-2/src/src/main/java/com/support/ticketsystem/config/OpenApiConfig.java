package com.support.ticketsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Ticket Support System API")
                .version("1.0.0")
                .description("Customer support ticket management system with multi-format import and auto-classification")
                .contact(new Contact()
                    .name("Support Team")
                    .email("support@example.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local development server")
            ));
    }
}
