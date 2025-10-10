package com.paystream.transferservice.domain;

/** Thrown when a business rule (domain) validation fails. */
public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) {
        super(message);
    }
}
