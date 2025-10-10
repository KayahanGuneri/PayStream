package com.paystream.paymentservice.domain;

// Enumerates valid states for the payment lifecycle.
public enum PaymentStatus {
    NEW,
    AUTH_PENDING,
    AUTH_APPROVED,
    AUTH_DECLINED,
    THREE_DS_REQUIRED,
    THREE_DS_VERIFIED,
    THREE_DS_FAILED,
    CAPTURED,
    REFUNDED,
    FAILED,
    VOIDED
}
