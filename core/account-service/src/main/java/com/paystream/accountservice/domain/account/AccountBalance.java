package com.paystream.accountservice.domain.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record AccountBalance(
        UUID accountId,
        BigDecimal currentBalance,
        Long asOfLedgerOffset,
        Instant updatedAt
){}

