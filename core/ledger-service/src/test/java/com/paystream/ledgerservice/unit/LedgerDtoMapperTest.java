package com.paystream.ledgerservice.unit;

import com.paystream.ledgerservice.app.mapper.LedgerDtoMapper;
import com.paystream.ledgerservice.api.BookTransactionRequest;
import com.paystream.ledgerservice.domain.LedgerEntry;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LedgerDtoMapperTest {

    @Test
    void toLedgerEntry_shouldMapFields() {
        UUID txId = UUID.randomUUID();

        BookTransactionRequest.Entry in = new BookTransactionRequest.Entry();
        in.setAccountId(UUID.randomUUID());
        in.setCurrency("TRY");
        in.setAmountMinor(-2500L);

        LedgerDtoMapper mapper = new LedgerDtoMapper();
        LedgerEntry out = mapper.toLedgerEntry(txId, 0, in);

        assertEquals(txId, out.getTxId());
        assertEquals(0, out.getTxSeq());
        assertEquals(in.getAccountId(), out.getAccountId());
        assertEquals("TRY", out.getCurrency());
        assertEquals(-2500L, out.getAmountMinor());
    }
}
