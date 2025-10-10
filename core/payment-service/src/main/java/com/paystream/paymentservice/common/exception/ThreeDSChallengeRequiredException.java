package com.paystream.paymentservice.common.exception;

// Business signal: a 3DS challenge must be completed before proceeding.
public class ThreeDSChallengeRequiredException extends PaymentException {
    public ThreeDSChallengeRequiredException(String message) { super(message); }
}
