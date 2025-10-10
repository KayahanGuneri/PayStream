package com.paystream.paymentservice.app.port;

// Tokenization/vault abstraction (no PAN storage)
public interface VaultTokenPort {
    boolean isValidToken(String cardToken);
}
