package com.paystream.paymentservice.infra.client;

import com.paystream.paymentservice.app.port.VaultTokenPort;
import org.springframework.stereotype.Component;

@Component // Registers a bean for VaultTokenPort
public class MockVaultTokenAdapter implements VaultTokenPort {

    @Override
    public boolean isValidToken(String cardToken) {
        // Dev rule: must start with "tok_" and be reasonably long
        return cardToken != null && cardToken.startsWith("tok_") && cardToken.length() >= 12;
    }
}
