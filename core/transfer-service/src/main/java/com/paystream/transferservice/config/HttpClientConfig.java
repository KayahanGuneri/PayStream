package com.paystream.transferservice.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Plain HTTP client config (no service discovery).
 * - Exposes a single RestClient.Builder bean
 * - Sets small connect/read timeouts
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        // Set basic timeouts
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(3))
                .withReadTimeout(Duration.ofSeconds(5));

        // Return a plain builder (no @LoadBalanced)
        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings));
    }
}
