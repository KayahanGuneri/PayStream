package com.paystream.ledgerservice.app.mapper;

import com.paystream.ledgerservice.api.BookTransactionRequest;
import com.paystream.ledgerservice.domain.LedgerEntry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component // Exposes this mapper as a Spring bean for DI and testability
public class LedgerDtoMapper {

    // Converts a single request line + contextual txId/seq into a LedgerEntry domain object
    public LedgerEntry toLedgerEntry(UUID txId, int seq, BookTransactionRequest.Entry in) {
        // We deliberately generate entryId here; persistence layer will assign ledgerOffset later
        return LedgerEntry.builder()
                .entryId(UUID.randomUUID())     // unique id per ledger line
                .txId(txId)                     // correlates all lines of the same business transaction
                .txSeq(seq)                     // in-transaction order; (tx_id, tx_seq) is UNIQUE in DB
                .accountId(in.getAccountId())   // target account for this line
                .currency(in.getCurrency())     // single-currency invariant is validated at app layer
                .amountMinor(in.getAmountMinor()) // signed minor units: +debit, -credit
                .build();                       // createdAt & ledgerOffset will be set by DB/application later
    }
}
