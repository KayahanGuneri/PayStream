package com.paystream.paymentservice.api.dto;

import com.paystream.paymentservice.domain.PaymentStatus;

import java.util.UUID;

public record AuthorizeResponse(
        UUID paymentId,
        PaymentStatus status
) {
    public static AuthorizeResponse of(UUID id, PaymentStatus status) {
        return new AuthorizeResponse(id, status);
    }
}
