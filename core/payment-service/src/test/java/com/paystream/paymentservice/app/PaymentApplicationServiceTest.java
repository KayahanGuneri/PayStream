package com.paystream.paymentservice.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paystream.paymentservice.api.dto.AuthorizeRequest;
import com.paystream.paymentservice.app.impl.PaymentApplicationService;
import com.paystream.paymentservice.infra.dao.JdbcOutboxPort;
import com.paystream.paymentservice.infra.dao.JdbcPaymentIntentRepository;
import com.paystream.paymentservice.infra.dao.JdbcPaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PURPOSE:
 * Unit-test the orchestration in PaymentApplicationService with pure mocks.
 * We verify the service calls repositories/ports with expected shapes.
 *
 * NOTES for beginners:
 * - Do not use matchers (any(), eq(), …) OUTSIDE when()/verify().
 * - When you use a matcher in a call, use matchers for ALL arguments of that call.
 */
@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

    @Mock JdbcPaymentRepository paymentRepo;
    @Mock JdbcPaymentIntentRepository intentRepo;
    @Mock JdbcOutboxPort outboxPort;
    @Mock ObjectMapper objectMapper;

    @InjectMocks
    PaymentApplicationService service;

    @Test
    @DisplayName("authorize(): persists payment and publishes outbox (happy path)")
    void authorize_happy_path() throws Exception {
        var req  = new AuthorizeRequest(
                UUID.randomUUID().toString(),
                new BigDecimal("100.00"),
                "TRY",
                "card-token-xyz"
        );
        var idem = "idem-123";

        // Stub mapper
        when(objectMapper.createObjectNode())
                .thenAnswer(inv -> JsonNodeFactory.instance.objectNode());
        when(objectMapper.writeValueAsString(any(ObjectNode.class)))
                .thenReturn("{\"ok\":true}");

        // Act
        service.authorize(req, idem);

        // Assert
        verify(paymentRepo, atLeastOnce()).savePayment(
                any(UUID.class),
                any(UUID.class),
                eq(new BigDecimal("100.00")),
                eq("TRY"),
                any(),
                eq("card-token-xyz"),
                eq("idem-123")
        );


        verify(intentRepo).insertAuthorizeIntent(
                any(UUID.class),
                eq(new BigDecimal("100.00")),
                eq(false),
                eq("")
        );

        verify(outboxPort, atLeastOnce()).publishEvent(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );

    }


    @Test
    @DisplayName("authorize(): idempotent (conceptual) with same key")
    void authorize_idempotent_basic() throws Exception {
        // Arrange
        var req = new AuthorizeRequest(UUID.randomUUID().toString(), new BigDecimal("10.00"), "USD", "card-t-1");
        var sameKey = "same-key";

        // Stub mapper again (her test izoledir)
        ObjectNode headers = JsonNodeFactory.instance.objectNode();
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        when(objectMapper.createObjectNode()).thenReturn(headers, payload);
        when(objectMapper.writeValueAsString(any(ObjectNode.class))).thenReturn("{\"ok\":true}");

        // Act
        service.authorize(req, sameKey);
        service.authorize(req, sameKey);

        // Assert
        verify(paymentRepo, atLeastOnce()).savePayment(
                any(UUID.class),
                any(UUID.class),
                eq(new BigDecimal("10.00")),
                eq("USD"),
                any(),
                eq("card-t-1"),
                eq("same-key")
        );
        verify(outboxPort, atLeastOnce()).publishEvent(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }
}
