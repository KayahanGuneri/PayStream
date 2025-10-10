package com.paystream.paymentservice.common.exception;

// Thrown when an Idempotency-Key is reused with a conflicting payload.
public class DuplicateRequestException extends PaymentException {
    public DuplicateRequestException(String message) { super(message); }
}
