package com.paystream.aiassistservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;



@SpringBootApplication


@EnableDiscoveryClient
public class AiAssistServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(AiAssistServiceApplication.class, args);
  }
}
