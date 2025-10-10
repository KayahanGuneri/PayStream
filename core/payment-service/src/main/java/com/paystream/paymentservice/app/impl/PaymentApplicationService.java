package com.paystream.paymentservice.app.impl;

import com.paystream.paymentservice.app.PaymentUseCase;
import com.paystream.paymentservice.app.command.*;
import com.paystream.paymentservice.app.port.*;
import com.paystream.paymentservice.app.provider.PaymentProviderPort;
import com.paystream.paymentservice.common.exception.*;
import com.paystream.paymentservice.infra.config.PaymentRiskProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service // Application service (use-case orchestrator)
public class PaymentApplicationService implements PaymentUseCase {

    // Ports (DIP): application depends on abstractions, not implementations
    private final PaymentRepository paymentRepo;
    private final PaymentIntentRepository intentRepo;
    private final PaymentCaptureRepository captureRepo;
    private final VaultTokenPort vault;
    private final ThreeDSSessionPort threeDS;
    private final OutboxPort outbox;
    private final PaymentProviderPort provider;

    // Config-driven risk decision (toggle + threshold)
    private final PaymentRiskProperties riskProps;

    public PaymentApplicationService(PaymentRepository paymentRepo,
                                     PaymentIntentRepository intentRepo,
                                     PaymentCaptureRepository captureRepo,
                                     VaultTokenPort vault,
                                     ThreeDSSessionPort threeDS,
                                     OutboxPort outbox,
                                     PaymentProviderPort provider,
                                     PaymentRiskProperties riskProps) {
        // Constructor injection → easier testing and enforces immutability
        this.paymentRepo = paymentRepo;
        this.intentRepo = intentRepo;
        this.captureRepo = captureRepo;
        this.vault = vault;
        this.threeDS = threeDS;
        this.outbox = outbox;
        this.provider = provider;
        this.riskProps = riskProps;
    }

    @Override
    @Transactional // Atomic DB changes (keep external I/O short)
    public UUID authorize(AuthorizeCommand cmd) {
        // 1) Idempotency guard: (merchantId, idempotencyKey)
        Optional<PaymentRepository.PaymentRecord> existing =
                paymentRepo.findByIdemKey(cmd.merchantId(), cmd.idempotencyKey());
        if (existing.isPresent()) {
            return UUID.fromString(existing.get().id());
        }

        // 2) Basic validation (vault token; amount format)
        if (!vault.isValidToken(cmd.cardToken())) {
            throw new InvalidTokenException("Invalid card token");
        }
        BigDecimal normalizedAmount = normalize(cmd.amount());

        // 3) Persist payment in AUTH_PENDING
        Instant now = Instant.now();
        UUID paymentId = paymentRepo.create(
                cmd.merchantId(), normalizedAmount, cmd.currency(),
                cmd.cardToken(), cmd.idempotencyKey(), "AUTH_PENDING", now);

        // 4) Risk decision (config-driven) → may require 3DS
        boolean requires3ds = shouldRequire3ds(normalizedAmount);
        intentRepo.create(paymentId.toString(), normalizedAmount,
                requires3ds, requires3ds ? "HIGH_AMOUNT" : "LOW_RISK", now);

        if (requires3ds) {
            // Create 3DS challenge session (in-memory/Redis adapter decides how)
            String challengeId = UUID.randomUUID().toString();
            String code = "123456"; // mock challenge code for dev
            threeDS.createSession(paymentId.toString(), challengeId, code, Duration.ofSeconds(120), 3);

            paymentRepo.updateStatus(paymentId.toString(), "THREE_DS_REQUIRED", now);
            // Business signal: client must complete challenge
            throw new ThreeDSChallengeRequiredException("3DS required");
        }

        // 5) Provider authorize (sync mock); update status → AUTH_APPROVED
        String providerRef = provider.authorize(cmd.merchantId(), normalizedAmount, cmd.currency(), cmd.cardToken());
        paymentRepo.updateStatus(paymentId.toString(), "AUTH_APPROVED", now);

        // 6) Emit outbox event for downstream (fraud, ledger, notifications, ...)
        outbox.appendEvent("PAYMENT", paymentId.toString(), "payment.authorized.v1",
                Map.of("paymentId", paymentId.toString(),
                        "merchantId", cmd.merchantId(),
                        "amount", normalizedAmount.toPlainString(),
                        "currency", cmd.currency(),
                        "status", "AUTH_APPROVED",
                        "providerRef", providerRef),
                now);

        return paymentId;
    }

    @Override
    @Transactional
    public void confirm3ds(Confirm3DSCommand cmd) {
        // Verify challenge session; adapter is responsible for TTL/attempts
        boolean ok = threeDS.verifyAndConsume(cmd.paymentId(), cmd.challengeId(), cmd.code());
        if (!ok) throw new PaymentException("3DS verification failed");

        Instant now = Instant.now();
        paymentRepo.updateStatus(cmd.paymentId(), "THREE_DS_VERIFIED", now);

        // After a successful 3DS, we treat it as authorized (separate event type in real world)
        outbox.appendEvent("PAYMENT", cmd.paymentId(), "payment.authorized.v1",
                Map.of("paymentId", cmd.paymentId(),
                        "status", "AUTH_APPROVED",
                        "authorizedAt", now.toString()),
                now);
    }

    @Override
    @Transactional
    public UUID capture(CaptureCommand cmd) {
        Instant now = Instant.now();
        BigDecimal normalized = normalize(cmd.amount());

        // Delegate to provider and persist capture
        String providerRef = provider.capture(cmd.paymentId(), normalized);
        UUID captureId = captureRepo.create(cmd.paymentId(), normalized,
                "SUCCEEDED", providerRef, cmd.idempotencyKey(), now);

        outbox.appendEvent("PAYMENT", cmd.paymentId(), "payment.captured.v1",
                Map.of("paymentId", cmd.paymentId(),
                        "captureId", captureId.toString(),
                        "amount", normalized.toPlainString(),
                        "capturedAt", now.toString()),
                now);
        return captureId;
    }

    @Override
    @Transactional
    public UUID refund(RefundCommand cmd) {
        Instant now = Instant.now();
        BigDecimal normalized = normalize(cmd.amount());

        String providerRef = provider.refund(cmd.paymentId(), normalized);
        UUID refundId = captureRepo.create(cmd.paymentId(), normalized,
                "REFUNDED", providerRef, cmd.idempotencyKey(), now);

        outbox.appendEvent("PAYMENT", cmd.paymentId(), "payment.refunded.v1",
                Map.of("paymentId", cmd.paymentId(),
                        "refundId", refundId.toString(),
                        "amount", normalized.toPlainString(),
                        "refundedAt", now.toString()),
                now);
        return refundId;
    }

    // --- Helpers -------------------------------------------------------------

    // Config-driven 3DS decision: global toggle + threshold
    private boolean shouldRequire3ds(BigDecimal amount) {
        if (!riskProps.isThreeDsEnabled()) return false;
        return amount != null && amount.compareTo(riskProps.getAmountThreshold()) >= 0;
    }

    // Normalizes monetary value to scale=2 with HALF_EVEN rule and validates > 0
    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) throw new PaymentValidationException("Amount is required");
        BigDecimal normalized = amount.setScale(2, java.math.RoundingMode.HALF_EVEN);
        if (normalized.signum() <= 0) {
            throw new PaymentValidationException("Amount must be positive");
        }
        return normalized;
    }
}
