package com.paystream.transferservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Central HTTP client configuration.
 * Exposes a single LoadBalanced RestClient.Builder bean (Eureka-aware).
 */
@Configuration
public class HttpClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        // Keep it simple; timeouts eklemek istersen ileride eklersin.
        return RestClient.builder();
    }
}
