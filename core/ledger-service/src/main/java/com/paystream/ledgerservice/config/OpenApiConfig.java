package com.paystream.ledgerservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Ledger Service API",
                version = "v1",
                description = "Internal API to append double-entry ledger transactions"
        )
)
public class OpenApiConfig {
    // No code needed; annotations customize the Swagger UI header.
}
