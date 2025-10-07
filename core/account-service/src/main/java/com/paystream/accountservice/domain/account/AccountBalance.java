package com.paystream.accountservice.domain.account;

import java.time.Instant;
import java.util.UUID;

public record AccountBalance(
        UUID accountId,
        long balanceMinor,     // <- BIGINT minor units
        Long asOfLedgerOffset,
        Instant updatedAt
) {}
