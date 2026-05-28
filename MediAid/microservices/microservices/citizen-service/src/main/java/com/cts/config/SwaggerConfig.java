package com.cts.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info=@Info(title = "MediAid API",version="1.0",description = "Citizen verification and document management system"))
public class SwaggerConfig {

}


//http://localhost:8009/swagger-ui/index.html 

