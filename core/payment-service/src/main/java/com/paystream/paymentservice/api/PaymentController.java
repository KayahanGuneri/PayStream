package com.paystream.paymentservice.api;

import com.paystream.paymentservice.api.dto.*;
import com.paystream.paymentservice.app.PaymentUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Exposes HTTP endpoints (REST)
@RequestMapping("/v1/payments") // API version & resource base path
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        // DIP: depend on an abstraction, not on a concrete service
        this.paymentUseCase = paymentUseCase;
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizeResponse> authorize(@RequestBody AuthorizeRequest req,
                                                       @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        // Stub implementation: returns a placeholder response
        // Later: delegate to paymentUseCase.authorize(req, idemKey)
        return ResponseEntity.ok(AuthorizeResponse.stub());
    }

    @PostMapping("/{paymentId}/confirm-3ds")
    public ResponseEntity<Void> confirm3ds(@PathVariable String paymentId,
                                           @RequestBody Confirm3DSRequest req) {
        // Stub: later will validate challenge code and progress FSM
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<Void> capture(@PathVariable String paymentId,
                                        @RequestBody CaptureRequest req,
                                        @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        // Stub: will call provider capture and persist outbox
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Void> refund(@PathVariable String paymentId,
                                       @RequestBody RefundRequest req,
                                       @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        // Stub: will validate refundable amount and publish refund event
        return ResponseEntity.ok().build();
    }
}
