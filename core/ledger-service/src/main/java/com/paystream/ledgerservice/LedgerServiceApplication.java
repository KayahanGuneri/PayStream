package com.paystream.ledgerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling // Enables @Scheduled tasks such as the OutboxRelay

public class LedgerServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(LedgerServiceApplication.class, args);
  }
}
