// Customizes OpenAPI/Swagger UI via springdoc-openapi.
// The UI will be available at /swagger-ui.html (configured by springdoc).
package com.paystream.transferservice.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // Builds an OpenAPI bean to customize title, version, description and security.
    @Bean
    public OpenAPI transferServiceOpenAPI() {
        // Optional bearer auth (e.g., when you integrate with Keycloak later)
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("PayStream — Transfer Service API")
                        .description("""
                                Endpoints for creating and querying money transfers.
                                Idempotency-Key is required for POST /v1/transfers.
                                """)
                        .version("v1")
                        .contact(new Contact().name("PayStream Team").email("team@paystream.local"))
                        .license(new License().name("Apache-2.0")))
                ;
    }

package com.paystream.transferservice.infra.config;

public class SwaggerConfig {

}
