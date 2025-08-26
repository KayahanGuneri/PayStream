package com.paystream.transferservice.infra.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class EurekaConfig {

    @Bean
    @LoadBalanced // resolves http://<service-id>/... via Eureka + LoadBalancer
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
