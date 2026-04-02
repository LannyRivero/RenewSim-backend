package com.renewsim.backend.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI renewSimOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RenewSim API")
                        .description("REST API for renewable energy simulation platform. " +
                                "Provides endpoints for authentication, user management, " +
                                "technology catalog, and energy simulation workflows.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RenewSim Dev Team")
                                .url("https://github.com/RenewSim")
                                .email("support@renewsim.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.renewsim.com").description("Production Server")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("RenewSim GitHub Repository")
                        .url("https://github.com/RenewSim"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/v1/auth/login endpoint")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}