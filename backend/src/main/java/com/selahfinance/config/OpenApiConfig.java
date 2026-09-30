package com.selahfinance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contrato OpenAPI publicado en /v3/api-docs y /swagger-ui.html.
 * Los otros equipos generan sus clientes a partir de este contrato.
 */
@Configuration
public class OpenApiConfig {

    static final String BEARER = "bearerJwt";
    static final String API_KEY = "apiKey";

    @Bean
    OpenAPI selahOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SelahFinance API")
                        .version("v1")
                        .description("Mayordomía cristiana: Tiempo, Talento, Tesoro y Templo."))
                .components(new Components()
                        .addSecuritySchemes(BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes(API_KEY, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-API-Key")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    @Bean
    GroupedOpenApi appMovilApi() {
        return GroupedOpenApi.builder()
                .group("app-movil")
                .pathsToMatch("/api/v1/**")
                .pathsToExclude("/api/v1/integraciones/**")
                .build();
    }

    @Bean
    GroupedOpenApi integracionesApi() {
        return GroupedOpenApi.builder()
                .group("integraciones")
                .pathsToMatch("/api/v1/integraciones/**")
                .addOpenApiCustomizer(api -> api.setSecurity(java.util.List.of(new SecurityRequirement().addList(API_KEY))))
                .build();
    }
}
