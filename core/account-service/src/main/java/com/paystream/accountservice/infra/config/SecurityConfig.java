package com.paystream.accountservice.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Centralized Spring Security configuration.
 * - CSRF is disabled for simplicity (enable it if you serve browsers & have stateful sessions).
 * - Public endpoints are whitelisted (Swagger, Actuator, and your public APIs).
 * - A BCrypt PasswordEncoder bean is provided for hashing customer passwords.
 *
 * NOTE:
 * For production, consider flipping `.anyRequest().permitAll()` to `.anyRequest().authenticated()`
 * and add real authentication (JWT, session, etc.).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Public endpoints that should not require authentication
    private static final String[] PUBLIC_ENDPOINTS = new String[] {
            // --- your APIs ---
            "/v1/customers",                 // POST (register)
            "/v1/customers/*",               // GET by id
            "/v1/customers/**",              // future subpaths (e.g., /{id}/accounts)
            "/v1/accounts/**",               // account read APIs if any
            "/accounts/**",                  // backward-compat (old path if exists)

            // --- Swagger / OpenAPI ---
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",

            // --- Actuator ---
            "/actuator/**",

            // --- Dev helpers (uncomment if you use H2 console) ---
            // "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless APIs; enable if needed.
                .csrf(csrf -> csrf.disable())

                // Allow frames for H2 console if you use it (safe to keep, does nothing if not in use)
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))

                .authorizeHttpRequests(auth -> auth
                        // Preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Everything else (DEV: open; PROD: consider authenticated())
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    /**
     * BCrypt password encoder for hashing customer passwords.
     * Default strength=10 is OK; you can tune it if needed.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
