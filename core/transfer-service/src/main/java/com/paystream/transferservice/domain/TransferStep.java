package com.paystream.transferservice.domain;

import java.util.UUID;
import java.time.OffsetDateTime;

/** Immutable audit record for a state transition (SRP: single purpose). */
public record TransferStep(
        UUID id,
        UUID transferId,
        TransferStatus from,
        TransferStatus to,
        String reason,
        OffsetDateTime createdAt
) {}
