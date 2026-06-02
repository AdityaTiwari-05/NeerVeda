package com.neerveda.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 📖 OpenApiConfig — Swagger / OpenAPI 3 configuration.
 *
 * Available at: /swagger-ui
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI neerVedaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NeerVeda API")
                .description("""
                    ## 💧 NeerVeda — Smart Water Safety & Disease Prevention System
                    
                    **Team:** CORE_401 | **Problem ID:** SIH25001
                    
                    ### Authentication
                    All endpoints (except `/api/v1/auth/**` and health checks) require a **Bearer JWT token**.
                    
                    1. `POST /api/v1/auth/login` → get `accessToken`
                    2. Add `Authorization: Bearer <token>` header to all requests
                    
                    ### Roles
                    - **ADMIN** — Full system access
                    - **GOVERNMENT_OFFICER** — View all data, manage alerts
                    - **HEALTH_WORKER** — Submit/view symptom reports
                    - **WATER_INSPECTOR** — Submit sensor readings
                    - **PUBLIC_VIEWER** — Read-only dashboard
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Team CORE_401")
                    .email("neerveda@sih2025.gov.in"))
                .license(new License()
                    .name("Government of India — Smart India Hackathon 2025")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter your JWT access token")));
    }
}
