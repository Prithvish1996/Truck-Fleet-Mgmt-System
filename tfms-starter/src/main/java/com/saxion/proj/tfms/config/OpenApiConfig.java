package com.saxion.proj.tfms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TFMS - Truck Fleet Management System API")
                        .version("1.0.0-dev")
                        .description("REST API documentation for the Truck Fleet Management System. " +
                                   "This API provides endpoints for managing truck fleet operations, " +
                                   "including system monitoring, configuration management, and metrics.")
                        .contact(new Contact()
                                .name("TFMS Development Team")
                                .email("dev@tfms.com")
                                .url("https://github.com/your-org/tfms"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.tfms.com")
                                .description("Production Server")
                ));
    }
}
