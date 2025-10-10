package com.paystream.paymentservice.common.exception;

// Base type for all payment-related runtime exceptions.
// SRP: Carries only error semantics, no handling logic.
public class PaymentException extends RuntimeException {
    public PaymentException(String message) { super(message); }
    public PaymentException(String message, Throwable cause) { super(message, cause); }
}
