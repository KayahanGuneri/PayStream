package com.paystream.accountservice.api.error;

public class ConflictException extends ApiException {
    public ConflictException(String message) { super(message); }
}
