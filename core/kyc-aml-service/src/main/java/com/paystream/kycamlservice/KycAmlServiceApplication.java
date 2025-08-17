package com.paystream.kycamlservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;



@SpringBootApplication


@EnableDiscoveryClient
public class KycAmlServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(KycAmlServiceApplication.class, args);
  }
}
