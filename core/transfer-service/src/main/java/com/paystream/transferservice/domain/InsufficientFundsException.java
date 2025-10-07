package com.paystream.transferservice.domain;

/** Business-level insufficient funds error coming from ledger/account checks. */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
