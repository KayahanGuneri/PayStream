// Converts domain models to API DTOs (and vice versa if needed).
// Keeps the controller thin and consistent.
package com.paystream.transferservice.api.mapper;

import com.paystream.transferservice.api.TransferResponse;
import com.paystream.transferservice.domain.Transfer;

public final class TransferApiMapper {

    private TransferApiMapper() {}

    // Domain -> API Response
    public static TransferResponse toResponse(Transfer t) {
        return new TransferResponse(t.id, t.status, t.ledgerTxId);
    }
}
