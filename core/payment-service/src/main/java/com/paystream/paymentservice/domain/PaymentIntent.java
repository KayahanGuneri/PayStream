package com.paystream.paymentservice.domain;
import java.math.BigDecimal;
import java.util.UUID;
public record PaymentIntent(UUID id, BigDecimal amount, String currency, String status) {}
