package com.paystream.paymentservice.infra.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PURPOSE (why this test exists):
 * Unit-test for the in-memory 3DS session adapter that implements ThreeDSSessionPort.
 * We verify the basic behaviors:
 *   1) Happy path: verifyAndConsume returns true and consumes the session
 *   2) Wrong code decreases attempts and eventually removes the session
 *   3) Expiration (TTL) makes verification fail
 *
 * NOTES for beginners:
 * - This is a pure unit test (no Spring, no database).
 * - We just "new" the adapter and call its methods.
 * - verifyAndConsume(...) returns boolean: true = success, false = fail.
 */
class InMemoryThreeDSSessionAdapterTest {

    @Test
    @DisplayName("Happy path: correct (paymentId, challengeId, code) -> true and session consumed")
    void verify_success_then_consumed() {
        // Arrange
        var adapter = new InMemoryThreeDSSessionAdapter();
        String paymentId = "p-1";
        String challengeId = "ch-1";
        String code = "123456";

        // Create a session with TTL 1 minute and 3 attempts
        adapter.createSession(paymentId, challengeId, code, Duration.ofMinutes(1), 3);

        // Act + Assert (first verification should succeed)
        boolean ok = adapter.verifyAndConsume(paymentId, challengeId, code);
        assertThat(ok).as("First verification should succeed").isTrue();

        // After a successful verification, session should be consumed (idempotent success)
        boolean secondTry = adapter.verifyAndConsume(paymentId, challengeId, code);
        assertThat(secondTry).as("Session should be consumed and not reusable").isFalse();
    }

    @Test
    @DisplayName("Wrong code decreases attempts; after attempts are exhausted session is removed")
    void wrong_code_exhausts_attempts() {
        // Arrange
        var adapter = new InMemoryThreeDSSessionAdapter();
        String paymentId = "p-2";
        String challengeId = "ch-xyz";
        String correctCode = "999999";

        // Start with exactly 2 attempts
        adapter.createSession(paymentId, challengeId, correctCode, Duration.ofMinutes(1), 2);

        // Act + Assert: first wrong attempt -> false (and attempts become 1)
        boolean firstWrong = adapter.verifyAndConsume(paymentId, challengeId, "000000");
        assertThat(firstWrong).as("First wrong attempt should fail").isFalse();

        // Second wrong attempt -> false and attempts become 0 -> session removed
        boolean secondWrong = adapter.verifyAndConsume(paymentId, challengeId, "111111");
        assertThat(secondWrong).as("Second wrong attempt should also fail and remove session").isFalse();

        // Now any further try (even with correct code) should fail because session is gone
        boolean afterExhausted = adapter.verifyAndConsume(paymentId, challengeId, correctCode);
        assertThat(afterExhausted).as("After attempts exhausted, session should not exist").isFalse();
    }

    @Test
    @DisplayName("Wrong challengeId fails without consuming attempts when ID does not match")
    void wrong_challenge_id_does_not_match() {
        // Arrange
        var adapter = new InMemoryThreeDSSessionAdapter();
        String paymentId = "p-3";
        String correctChallengeId = "ch-ok";
        String correctCode = "1234";

        adapter.createSession(paymentId, correctChallengeId, correctCode, Duration.ofMinutes(1), 2);

        // Act: use a different challengeId
        boolean result = adapter.verifyAndConsume(paymentId, "ch-wrong", correctCode);

        // Assert
        assertThat(result).as("Wrong challengeId should fail").isFalse();

        // We can still succeed with the correct pair (session is still there because wrong ID doesn't decrement attempts)
        boolean nowSuccess = adapter.verifyAndConsume(paymentId, correctChallengeId, correctCode);
        assertThat(nowSuccess).as("Correct challengeId + code should still pass").isTrue();
    }

    @Test
    @DisplayName("TTL expiration: after TTL passes, verification should fail and session removed")
    void ttl_expiration_fails() throws InterruptedException {
        // Arrange
        var adapter = new InMemoryThreeDSSessionAdapter();
        String paymentId = "p-4";
        String challengeId = "ch-ttl";
        String code = "5555";

        // Very small TTL (10 ms) to force expiration quickly
        adapter.createSession(paymentId, challengeId, code, Duration.ofMillis(10), 2);

        // Wait long enough to pass TTL
        Thread.sleep(20);

        // Act
        boolean result = adapter.verifyAndConsume(paymentId, challengeId, code);

        // Assert
        assertThat(result).as("After TTL, verification should fail").isFalse();

        // And another try is also false because session was removed on TTL check
        boolean again = adapter.verifyAndConsume(paymentId, challengeId, code);
        assertThat(again).isFalse();
    }
}
