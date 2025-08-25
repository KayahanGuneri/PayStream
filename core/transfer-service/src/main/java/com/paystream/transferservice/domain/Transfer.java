
// Minimal domain entity for persistence + business transitions
package com.paystream.transferservice.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Transfer {
    public UUID id;
    public UUID sourceAccountId;
    public UUID destAccountId;
    public String currency;
    public long amountMinor;
    public String idempotencyKey;
    public TransferStatus status;
    public UUID ledgerTxId;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;

    // Simple helper to create a new pending transfer
    public static Transfer pending(UUID id, UUID source, UUID dest, String currency, long amount, String idemKey) {
        var t = new Transfer();
        t.id = id;
        t.sourceAccountId = source;
        t.destAccountId = dest;
        t.currency = currency.toUpperCase(); // normalize to ISO-4217 uppercase
        t.amountMinor = amount;
        t.idempotencyKey = idemKey;
        t.status = TransferStatus.PENDING;
        return t;
    }

package com.paystream.transferservice.domain;

public class Transfer {

}
