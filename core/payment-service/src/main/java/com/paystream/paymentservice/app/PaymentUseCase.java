package com.paystream.paymentservice.app;

import com.paystream.paymentservice.app.command.*;
import java.util.UUID;

// Clean boundary: web layer depends on this interface, not on implementations
public interface PaymentUseCase {

    // Idempotent authorization flow entry point
    UUID authorize(AuthorizeCommand cmd);

    // Confirms a 3DS challenge (idempotent success)
    void confirm3ds(Confirm3DSCommand cmd);

    // Creates a (partial) capture and returns captureId
    UUID capture(CaptureCommand cmd);

    // Creates a (partial) refund and returns refundId
    UUID refund(RefundCommand cmd);
}
