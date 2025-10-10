package com.paystream.paymentservice.app.impl;

import com.paystream.paymentservice.app.PaymentUseCase;
import com.paystream.paymentservice.app.command.*;
import com.paystream.paymentservice.app.port.*;
import com.paystream.paymentservice.app.provider.PaymentProviderPort;
import com.paystream.paymentservice.common.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service // Registers this class as an application service
public class PaymentApplicationService implements PaymentUseCase {

    private final PaymentRepository paymentRepo;
    private final PaymentIntentRepository intentRepo;
    private final PaymentCaptureRepository captureRepo;
    private final VaultTokenPort vault;
    private final ThreeDSSessionPort threeDS;
    private final OutboxPort outbox;
    private final PaymentProviderPort provider;

    public PaymentApplicationService(PaymentRepository paymentRepo,
                                     PaymentIntentRepository intentRepo,
                                     PaymentCaptureRepository captureRepo,
                                     VaultTokenPort vault,
                                     ThreeDSSessionPort threeDS,
                                     OutboxPort outbox,
                                     PaymentProviderPort provider) {
        // DIP: constructor injection of ports keeps this class infrastructure-agnostic
        this.paymentRepo = paymentRepo;
        this.intentRepo = intentRepo;
        this.captureRepo = captureRepo;
        this.vault = vault;
        this.threeDS = threeDS;
        this.outbox = outbox;
        this.provider = provider;
    }

    @Override
    @Transactional // Atomic DB changes; prefer external I/O out of the TX for real impl
    public UUID authorize(AuthorizeCommand cmd) {
        // Idempotent lookup by (merchantId, idempotencyKey)
        Optional<PaymentRepository.PaymentRecord> existing =
                paymentRepo.findByIdemKey(cmd.merchantId(), cmd.idempotencyKey());
        if (existing.isPresent()) {
            return UUID.fromString(existing.get().id());
        }

        // Token validation via vault (no PAN storage)
        if (!vault.isValidToken(cmd.cardToken())) {
            throw new InvalidTokenException("Invalid card token");
        }

        Instant now = Instant.now();
        UUID paymentId = paymentRepo.create(
                cmd.merchantId(), normalize(cmd.amount()), cmd.currency(),
                cmd.cardToken(), cmd.idempotencyKey(), "AUTH_PENDING", now);

        // Minimal RBA: assume low risk (no 3DS) for skeleton
        boolean requires3ds = false;
        intentRepo.create(paymentId.toString(), cmd.amount(), requires3ds, "LOW_RISK", now);

        if (requires3ds) {
            String challengeId = UUID.randomUUID().toString();
            String code = "123456";
            threeDS.createSession(paymentId.toString(), challengeId, code, Duration.ofSeconds(120), 3);
            paymentRepo.updateStatus(paymentId.toString(), "THREE_DS_REQUIRED", now);
            throw new ThreeDSChallengeRequiredException("3DS required");
        }

        // Provider authorize (sync stub)
        String providerRef = provider.authorize(cmd.merchantId(), cmd.amount(), cmd.currency(), cmd.cardToken());
        paymentRepo.updateStatus(paymentId.toString(), "AUTH_APPROVED", now);

        outbox.appendEvent("PAYMENT", paymentId.toString(), "payment.authorized.v1",
                Map.of("paymentId", paymentId.toString(),
                        "merchantId", cmd.merchantId(),
                        "amount", cmd.amount().toPlainString(),
                        "currency", cmd.currency(),
                        "status", "AUTH_APPROVED",
                        "providerRef", providerRef), now);

        return paymentId;
    }

    @Override
    @Transactional
    public void confirm3ds(Confirm3DSCommand cmd) {
        boolean ok = threeDS.verifyAndConsume(cmd.paymentId(), cmd.challengeId(), cmd.code());
        if (!ok) throw new PaymentException("3DS verification failed");
        Instant now = Instant.now();
        paymentRepo.updateStatus(cmd.paymentId(), "THREE_DS_VERIFIED", now);
        outbox.appendEvent("PAYMENT", cmd.paymentId(), "payment.authorized.v1",
                Map.of("paymentId", cmd.paymentId(),
                        "status", "AUTH_APPROVED",
                        "authorizedAt", now.toString()), now);
    }

    @Override
    @Transactional
    public UUID capture(CaptureCommand cmd) {
        Instant now = Instant.now();
        String providerRef = provider.capture(cmd.paymentId(), normalize(cmd.amount()));
        UUID captureId = captureRepo.create(cmd.paymentId(), normalize(cmd.amount()),
                "SUCCEEDED", providerRef, cmd.idempotencyKey(), now);

        outbox.appendEvent("PAYMENT", cmd.paymentId(), "payment.captured.v1",
                Map.of("paymentId", cmd.paymentId(),
                        "captureId", captureId.toString(),
                        "amount", cmd.amount().toPlainString(),
                        "capturedAt", now.toString()), now);
        return captureId;
    }

    @Override
    @Transactional
    public UUID refund(RefundCommand cmd) {
        Instant now = Instant.now();
        String providerRef = provider.refund(cmd.paymentId(), normalize(cmd.amount()));
        UUID refundId = captureRepo.create(cmd.paymentId(), normalize(cmd.amount()),
                "REFUNDED", providerRef, cmd.idempotencyKey(), now);

        outbox.appendEvent("PAYMENT", cmd.paymentId(), "payment.refunded.v1",
                Map.of("paymentId", cmd.paymentId(),
                        "refundId", refundId.toString(),
                        "amount", cmd.amount().toPlainString(),
                        "refundedAt", now.toString()), now);
        return refundId;
    }

    // Normalizes monetary value to scale=2 with HALF_EVEN rule
    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) throw new PaymentValidationException("Amount is required");
        return amount.setScale(2, java.math.RoundingMode.HALF_EVEN);
    }
}
