package com.paystream.paymentservice.infra.client;

import com.paystream.paymentservice.app.port.ThreeDSSessionPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component // Registers a bean for ThreeDSSessionPort
public class InMemoryThreeDSSessionAdapter implements ThreeDSSessionPort {

    private static class Session {
        final String challengeId;
        final String code;
        final Instant expiresAt;
        int attempts;
        Session(String challengeId, String code, Duration ttl, int attempts) {
            this.challengeId = challengeId;
            this.code = code;
            this.expiresAt = Instant.now().plus(ttl);
            this.attempts = attempts;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>(); // key: paymentId

    @Override
    public void createSession(String paymentId, String challengeId, String code, Duration ttl, int attempts) {
        sessions.put(paymentId, new Session(challengeId, code, ttl, attempts));
    }

    @Override
    public boolean verifyAndConsume(String paymentId, String challengeId, String code) {
        Session s = sessions.get(paymentId);
        if (s == null) return false;
        if (Instant.now().isAfter(s.expiresAt)) { sessions.remove(paymentId); return false; }
        if (!s.challengeId.equals(challengeId)) return false;
        if (!s.code.equals(code)) { if (--s.attempts <= 0) sessions.remove(paymentId); return false; }
        sessions.remove(paymentId); // idempotent success
        return true;
    }
}
