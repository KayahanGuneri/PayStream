package com.paystream.bankadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;



@SpringBootApplication


@EnableDiscoveryClient
public class BankAdapterApplication {
  public static void main(String[] args) {
    SpringApplication.run(BankAdapterApplication.class, args);
  }
}
