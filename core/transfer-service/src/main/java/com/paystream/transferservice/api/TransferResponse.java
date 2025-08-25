package com.paystream.transferservice.api;

import com.paystream.transferservice.domain.TransferStatus;
import java.util.UUID;


// API DTO for returning transfer information to clients
public record TransferResponse(
        UUID transferId,          // the server-generated transfer id
        TransferStatus status,    // PENDING / IN_PROGRESS / COMPLETED / FAILED
        UUID ledgerTxId           // id used when writing to Ledger (may be null if FAILED)
) {}
