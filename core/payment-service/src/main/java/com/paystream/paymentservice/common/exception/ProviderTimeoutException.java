package com.paystream.paymentservice.common.exception;

// Indicates upstream payment provider did not respond within SLA.
public class ProviderTimeoutException extends PaymentException {
    public ProviderTimeoutException(String message) { super(message); }
    public ProviderTimeoutException(String message, Throwable cause) { super(message, cause); }
}
