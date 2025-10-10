package com.paystream.paymentservice.infra.client;

import com.paystream.paymentservice.app.provider.PaymentProviderPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Primary // If later you add another provider, prefer this one by default
public class MockStripeProviderAdapter implements PaymentProviderPort {

    @Override
    public String authorize(String merchantId, BigDecimal amount, String currency, String cardToken) {
        return "pi_" + UUID.randomUUID();
    }

    @Override
    public String capture(String paymentId, BigDecimal amount) {
        return "cap_" + UUID.randomUUID();
    }

    @Override
    public String refund(String paymentId, BigDecimal amount) {
        return "rf_" + UUID.randomUUID();
    }
}
