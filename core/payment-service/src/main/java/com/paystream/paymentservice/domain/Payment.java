package com.paystream.paymentservice.domain;
import java.math.BigDecimal;
import java.util.UUID;
public record Payment(UUID id, String merchantId, BigDecimal amount, String currency, String status) {}
