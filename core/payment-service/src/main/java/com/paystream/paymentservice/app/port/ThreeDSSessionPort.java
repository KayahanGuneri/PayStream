package com.paystream.paymentservice.app.port;

import java.time.Duration;

// Redis-like store abstraction for 3DS sessions
public interface ThreeDSSessionPort {
    void createSession(String paymentId, String challengeId, String code, Duration ttl, int attempts);
    boolean verifyAndConsume(String paymentId, String challengeId, String code);
}
