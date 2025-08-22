package com.paystream.ledgerservice.infra.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@ConditionalOnProperty(value = "ledger.snapshot.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig { }
