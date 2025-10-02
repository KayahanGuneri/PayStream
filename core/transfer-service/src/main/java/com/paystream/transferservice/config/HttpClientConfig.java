package com.paystream.transferservice.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Central HTTP client configuration.
 * - Exposes a single LoadBalanced RestClient.Builder bean (Eureka-aware)
 * - Sets conservative connect/read timeouts
 * - Do NOT expose a plain RestClient bean here to avoid bean ambiguity
 */
@Configuration
public class HttpClientConfig {
    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder() {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(3))
                .withReadTimeout(Duration.ofSeconds(5));

        //Build a builder that will create clients using these defaults
        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings));
    }
}

