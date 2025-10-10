package com.paystream.paymentservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Binds to properties under "payment.risk" in application.yml
@Component
@ConfigurationProperties(prefix = "payment.risk")
public class PaymentRiskProperties {
    // Enable/disable 3DS globally (dev may disable)
    private boolean threeDsEnabled = true;

    // Simple threshold rule for demo; require 3DS when amount >= threshold
    private BigDecimal amountThreshold = new BigDecimal("1000.00");

    public boolean isThreeDsEnabled() { return threeDsEnabled; }
    public void setThreeDsEnabled(boolean threeDsEnabled) { this.threeDsEnabled = threeDsEnabled; }

    public BigDecimal getAmountThreshold() { return amountThreshold; }
    public void setAmountThreshold(BigDecimal amountThreshold) { this.amountThreshold = amountThreshold; }
}
