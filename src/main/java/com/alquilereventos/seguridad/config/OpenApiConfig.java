package com.alquilereventos.seguridad.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI seguridadOpenAPI(@Value("${app.openapi.server-url}") String serverUrl) {
        return new OpenAPI()
                .info(new Info().title("MS Seguridad").version("1.0.0")
                        .description("Login JWT, usuarios y roles."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .servers(List.of(new Server().url(serverUrl).description("API Gateway")));
    }
}
