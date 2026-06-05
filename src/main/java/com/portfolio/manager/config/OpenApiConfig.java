package com.portfolio.manager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Portfolio Manager API",
        version = "1.0",
        description = "API para gerenciamento de portfólio de projetos",
        contact = @Contact(name = "Equipe de desenvolvimento")))
public class OpenApiConfig {
}

