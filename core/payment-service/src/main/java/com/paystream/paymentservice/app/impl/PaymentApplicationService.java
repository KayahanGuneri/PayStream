package com.paystream.paymentservice.app.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paystream.paymentservice.api.dto.*;
import com.paystream.paymentservice.app.PaymentUseCase;
import com.paystream.paymentservice.domain.PaymentStatus;
import com.paystream.paymentservice.infra.dao.JdbcOutboxPort;
import com.paystream.paymentservice.infra.dao.JdbcPaymentIntentRepository;
import com.paystream.paymentservice.infra.dao.JdbcPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentApplicationService implements PaymentUseCase {

    private final JdbcPaymentRepository payments;
    private final JdbcPaymentIntentRepository intents;
    private final JdbcOutboxPort outbox;
    private final ObjectMapper objectMapper;

    public PaymentApplicationService(JdbcPaymentRepository payments,
                                     JdbcPaymentIntentRepository intents,
                                     JdbcOutboxPort outbox,
                                     ObjectMapper objectMapper) {
        this.payments = payments;
        this.intents = intents;
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuthorizeResponse authorize(AuthorizeRequest request, String idempotencyKey) {
        Objects.requireNonNull(request, "request must not be null");
        if (request.merchantId() == null || request.merchantId().isBlank()) {
            throw new IllegalArgumentException("merchantId is required");
        }
        if (request.currency() == null || request.currency().isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
        if (request.cardToken() == null || request.cardToken().isBlank()) {
            throw new IllegalArgumentException("cardToken is required");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        final UUID merchantUuid;
        try {
            merchantUuid = UUID.fromString(request.merchantId());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("merchantId must be a valid UUID");
        }

        final UUID paymentId = UUID.randomUUID();

        boolean requires3ds = request.amount().compareTo(new BigDecimal("500.00")) > 0;
        String rbaReason = requires3ds ? "AMOUNT_THRESHOLD" : "";


        payments.savePayment(
                paymentId,
                merchantUuid,
                request.amount(),
                request.currency(),
                PaymentStatus.AUTH_PENDING,
                request.cardToken(),
                idempotencyKey
        );

        intents.insertAuthorizeIntent(
                paymentId,
                request.amount(),
                requires3ds,
                rbaReason
        );

        // --- NULL-GÜVENLİ JSON ---
        ObjectNode payload = objectMapper.createObjectNode()
                .put("paymentId", paymentId.toString())
                .put("merchantId", merchantUuid.toString())
                .put("currency", request.currency())
                .put("requires3ds", requires3ds);
        payload.putPOJO("amount", request.amount()); // BigDecimal
        if (rbaReason != null) payload.put("rbaReason", rbaReason);

        ObjectNode headers = objectMapper.createObjectNode()
                .put("idempotencyKey", idempotencyKey);

        outbox.publishEvent(
                paymentId,
                "PAYMENT",
                "PAYMENT_AUTHORIZED",
                toJson(payload),
                toJson(headers)
        );

        return new AuthorizeResponse(paymentId, PaymentStatus.AUTH_PENDING);
    }

    @Override
    public void confirm3ds(String paymentId, Confirm3DSRequest request) {
        UUID pid = UUID.fromString(paymentId);

        if (request != null && request.amount() != null && request.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }

        payments.updateStatus(pid, PaymentStatus.AUTH_APPROVED);

        ObjectNode payload = objectMapper.createObjectNode()
                .put("paymentId", paymentId);
        if (request != null && request.amount() != null) {
            payload.putPOJO("amount", request.amount());
        }

        outbox.publishEvent(
                pid,
                "PAYMENT",
                "PAYMENT_3DS_CONFIRMED",
                toJson(payload),
                "{}"
        );
    }

    @Override
    public void capture(String paymentId, CaptureRequest request, String idempotencyKey) {
        UUID pid = UUID.fromString(paymentId);
        Optional<JdbcPaymentRepository.PaymentRecord> rec = payments.findById(pid);
        if (rec.isEmpty()) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        payments.updateStatus(pid, PaymentStatus.CAPTURED);

        ObjectNode payload = objectMapper.createObjectNode()
                .put("paymentId", paymentId);
        if (request != null && request.amount() != null) {
            payload.putPOJO("captureAmount", request.amount());
        }

        ObjectNode headers = objectMapper.createObjectNode()
                .put("idempotencyKey", idempotencyKey);

        outbox.publishEvent(
                pid,
                "PAYMENT",
                "PAYMENT_CAPTURED",
                toJson(payload),
                toJson(headers)
        );
    }

    @Override
    public void refund(String paymentId, RefundRequest request, String idempotencyKey) {
        UUID pid = UUID.fromString(paymentId);
        Optional<JdbcPaymentRepository.PaymentRecord> rec = payments.findById(pid);
        if (rec.isEmpty()) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        payments.updateStatus(pid, PaymentStatus.REFUNDED);

        ObjectNode payload = objectMapper.createObjectNode()
                .put("paymentId", paymentId);
        if (request != null) {
            if (request.code() != null) payload.put("code", request.code());
            if (request.message() != null) payload.put("message", request.message());
        }

        ObjectNode headers = objectMapper.createObjectNode()
                .put("idempotencyKey", idempotencyKey);

        outbox.publishEvent(
                pid,
                "PAYMENT",
                "PAYMENT_REFUNDED",
                toJson(payload),
                toJson(headers)
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }
}
