// Used when a requested transfer doesn't exist.
package com.paystream.transferservice.domain;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
