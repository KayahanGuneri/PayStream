package com.paystream.paymentservice.api;

import com.paystream.paymentservice.api.dto.*;
import com.paystream.paymentservice.app.PaymentUseCase;
import com.paystream.paymentservice.app.impl.PaymentApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;
    private final PaymentApplicationService app;


    public PaymentController(PaymentUseCase paymentUseCase, PaymentApplicationService app) {
        this.paymentUseCase = paymentUseCase;
        this.app = app;
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizeResponse> authorize(
            @Valid @RequestBody AuthorizeRequest request,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey
    ) {
        var result = app.authorize(request, idempotencyKey);
        return ResponseEntity.ok(result);
    }




    @PostMapping("/{paymentId}/confirm-3ds")
    public ResponseEntity<Void> confirm3ds(@PathVariable String paymentId,
                                           @RequestBody Confirm3DSRequest req) {
        paymentUseCase.confirm3ds(paymentId, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<Void> capture(@PathVariable String paymentId,
                                        @RequestBody CaptureRequest req,
                                        @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        paymentUseCase.capture(paymentId, req, idemKey);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Void> refund(@PathVariable String paymentId,
                                       @RequestBody RefundRequest req,
                                       @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        paymentUseCase.refund(paymentId, req, idemKey);
        return ResponseEntity.ok().build();
    }
}
