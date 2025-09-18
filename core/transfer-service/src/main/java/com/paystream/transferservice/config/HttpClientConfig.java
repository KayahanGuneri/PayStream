package com.paystream.transferservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    @Bean @LoadBalanced
    RestClient.Builder restClientBuilder() { return RestClient.builder(); }

    @Bean
    RestClient restClient(@LoadBalanced RestClient.Builder builder) { return builder.build(); }
}

