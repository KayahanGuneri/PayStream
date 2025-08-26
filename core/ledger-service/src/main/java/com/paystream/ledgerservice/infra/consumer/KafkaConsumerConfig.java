package com.paystream.ledgerservice.infra.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(
        value = "ledger.snapshot.consumer.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProps;
    private final ObjectProvider<SslBundles> sslBundlesProvider;

    public KafkaConsumerConfig(KafkaProperties kafkaProps,
                               ObjectProvider<SslBundles> sslBundlesProvider) {
        this.kafkaProps = kafkaProps;
        this.sslBundlesProvider = sslBundlesProvider;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> ledgerKafkaListenerContainerFactory() {
        // Spring config’ini temel al (bootstrap.servers dahil)
        Map<String,Object> props = new HashMap<>(
                kafkaProps.buildConsumerProperties(sslBundlesProvider.getIfAvailable())
        );

        // Ek ayarların
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 200);

        var cf = new DefaultKafkaConsumerFactory<String, String>(props);
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
