package com.paystream.paymentservice.app.command;

// Input model for confirming a 3DS challenge
public record Confirm3DSCommand(
        String paymentId,
        String challengeId,
        String code
) { }
