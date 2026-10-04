package com.org.ecommerce.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Backend API")
                        .version("v1")
                        .description("Production-ready E-Commerce Backend built with Spring Boot, JWT Authentication, JPA and PostgreSQL"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", //give a name for authentication - totally our choice
                                new SecurityScheme() //describe how authentication works
                                        .type(SecurityScheme.Type.HTTP) //to tell that authentication happens using HTTP headers
                                        .scheme("bearer") //
                                        .bearerFormat("JWT"))) //it tells humans that bearer token is actually a JWT
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication")); //
    }
}
