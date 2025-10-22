package com.paystream.paymentservice.app;

import com.paystream.paymentservice.api.dto.*;

public interface PaymentUseCase {
    AuthorizeResponse authorize(AuthorizeRequest request, String idempotencyKey);
    void confirm3ds(String paymentId, Confirm3DSRequest request);
    void capture(String paymentId, CaptureRequest request, String idempotencyKey);
    void refund(String paymentId, RefundRequest request, String idempotencyKey);
}
