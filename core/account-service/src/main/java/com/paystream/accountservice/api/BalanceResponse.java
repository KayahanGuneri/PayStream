package com.paystream.accountservice.api;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceResponse(UUID accountId, BigDecimal currentBalance, Long asOfLedgerOffset, Instant updatedAt) {}
