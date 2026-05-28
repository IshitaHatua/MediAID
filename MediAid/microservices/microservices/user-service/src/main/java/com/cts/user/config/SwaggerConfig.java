package com.cts.user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "MediAID User Service API", version = "1.0",
        description = "User management service - CRUD operations and role management"))
public class SwaggerConfig {
}
