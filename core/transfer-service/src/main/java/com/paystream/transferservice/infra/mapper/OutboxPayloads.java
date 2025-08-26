// Centralizes JSON payload building for outbox events.
// In real apps, prefer Jackson ObjectMapper over manual strings.
package com.paystream.transferservice.infra.mapper;

public final class OutboxPayloads {
    private OutboxPayloads() {}

    public static String transferCompleted(String transferId, String ledgerTxId) {
        return """
            {"type":"TRANSFER_COMPLETED","transferId":"%s","ledgerTxId":"%s"}
            """.formatted(transferId, ledgerTxId);
    }

    public static String transferFailed(String transferId, String reason) {
        return """
            {"type":"TRANSFER_FAILED","transferId":"%s","reason":"%s"}
            """.formatted(transferId, reason);
    }
}
