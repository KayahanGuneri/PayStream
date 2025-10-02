package com.paystream.transferservice.domain;

/**
 * Thrown when the same Idempotency-Key is used with a different request body.
 * Mapped to HTTP 409 (Conflict) by the global exception handler.
 */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
