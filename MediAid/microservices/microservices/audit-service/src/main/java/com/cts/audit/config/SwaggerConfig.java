package com.cts.audit.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "MediAID Audit Service API", version = "1.0",
        description = "Audit log service - stores and retrieves system activity logs"))
public class SwaggerConfig {
}
