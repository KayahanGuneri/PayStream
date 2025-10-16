package com.paystream.paymentservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Payment Service API",
                version = "v1",
                description = "Ödeme servisinin REST API dokümantasyonu"
        ),
        servers = {
                @Server(url = "/", description = "Default (container içi)"),
                @Server(url = "http://localhost:9095", description = "Local Docker")
        }
)
public class OpenApiConfig {}
