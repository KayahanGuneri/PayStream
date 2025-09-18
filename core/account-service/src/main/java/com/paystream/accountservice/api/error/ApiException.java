package com.paystream.accountservice.api.error;

/** Base type for domain/API errors. */
public abstract class ApiException extends RuntimeException {
    public ApiException(String message) { super(message); }
}
