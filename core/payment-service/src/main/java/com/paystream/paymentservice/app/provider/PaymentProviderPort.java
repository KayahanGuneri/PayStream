package com.paystream.paymentservice.app.provider;

import java.math.BigDecimal;

public interface PaymentProviderPort {

    String authorize(String merchantId, BigDecimal amount, String currency,String cardToken);
    String capture(String paymentId,BigDecimal amount);
    String refund(String paymentId,BigDecimal amount);
}
