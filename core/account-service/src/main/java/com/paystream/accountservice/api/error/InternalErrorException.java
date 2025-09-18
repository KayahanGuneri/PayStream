package com.paystream.accountservice.api.error;

public class InternalErrorException extends ApiException {
    public InternalErrorException(String message) { super(message); }
}
