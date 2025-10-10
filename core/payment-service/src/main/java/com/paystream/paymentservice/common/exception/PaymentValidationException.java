package com.paystream.paymentservice.common.exception;

// Thrown when request fails business validation rules (e.g., missing amount)
public class PaymentValidationException extends PaymentException {
    public PaymentValidationException(String message) { super(message); }
}
