package com.paystream.ledgerservice.domain;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class LedgerEntry {
    // Unique identifier for this ledger line
    private UUID entryId;

    // Transaction correlation: all lines of the same business transaction share the same txId
    private UUID txId;

    // Order within the transaction (0,1,2...); UNIQUE together with txId for idempotency
    private int txSeq;

    // The account impacted by this ledger line
    private UUID accountId;

    // Currency code (e.g., TRY, USD) - kept as TEXT in DB for simplicity
    private String currency;

    // Signed amount in minor units (debit = positive, credit = negative)
    private long amountMinor;

    // Assigned by DB at insert time from a global sequence; defines total ordering
    private Long ledgerOffset;

    // Server timestamp when inserted
    private OffsetDateTime createdAt;
}
