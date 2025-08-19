package com.paystream.accountservice.domain;

import java.time.Instant;
import java.util.UUID;

public record Account(
        UUID id,
        UUID customerId,
        String currency,
        String status,     // ACTIVE/BLOCKED/CLOSED
        long version,      // optimistic lock sayacı
        Instant createdAt,
        Instant updatedAt
) {}
