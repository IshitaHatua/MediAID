package com.cts.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "MediAID Auth Service API", version = "1.0",
        description = "Authentication and authorization service - login, register, password reset, JWT issuance"))
public class SwaggerConfig {
}
