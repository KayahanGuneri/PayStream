package com.paystream.ledgerservice.unit;


import com.paystream.ledgerservice.app.LedgerTransactionAppService;
import com.paystream.ledgerservice.api.BookTransactionRequest;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository;
import com.paystream.ledgerservice.infra.repo.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LedgerTransactionAppServiceValidationTest {

    @Mock
    LedgerEntryRepository ledgerRepo;

    @Mock
    OutboxRepository outboxRepo;

    @InjectMocks
    LedgerTransactionAppService service;

    @Test
    void emptyEntries_shouldThrow() {
        BookTransactionRequest req = new BookTransactionRequest();
        req.setTxId(UUID.randomUUID());
        req.setEntries(List.of());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.bookTransaction(req));
        assertTrue(ex.getMessage().toLowerCase().contains("entry"));
        verifyNoInteractions(ledgerRepo, outboxRepo);
    }


    @Test
    void mixedCurrencies_shouldThrow() {
        BookTransactionRequest.Entry e1 = new BookTransactionRequest.Entry();
        e1.setAccountId(UUID.randomUUID());
        e1.setCurrency("TRY");
        e1.setAmountMinor((long) -1000);

        BookTransactionRequest.Entry e2 = new BookTransactionRequest.Entry();
        e2.setAccountId(UUID.randomUUID());
        e2.setCurrency("USD");
        e2.setAmountMinor((long) +1000);

        BookTransactionRequest req = new BookTransactionRequest();
        req.setTxId(UUID.randomUUID());
        req.setEntries(List.of(e1, e2));


        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.bookTransaction(req));
        assertTrue(ex.getMessage().toLowerCase().contains("currency"));
        verifyNoInteractions(ledgerRepo, outboxRepo);

    }

    @Test
    void sumNotZero_shouldThrow() {
        BookTransactionRequest.Entry d = new BookTransactionRequest.Entry();
        d.setAccountId(UUID.randomUUID());
        d.setCurrency("TRY");
        d.setAmountMinor((long) +1000);

        BookTransactionRequest req = new BookTransactionRequest();
        req.setTxId(UUID.randomUUID());
        req.setEntries(List.of(d));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.bookTransaction(req));
        assertTrue(ex.getMessage().toLowerCase().contains("sum"));
        verifyNoInteractions(ledgerRepo, outboxRepo);
    }


    @Test
    void happyPath_shouldCallRepos() {
        BookTransactionRequest.Entry c = new BookTransactionRequest.Entry();
        c.setAccountId(UUID.randomUUID());
        c.setCurrency("TRY");
        c.setAmountMinor((long) -1000);

        BookTransactionRequest.Entry d = new BookTransactionRequest.Entry();
        d.setAccountId(UUID.randomUUID());
        d.setCurrency("TRY");
        d.setAmountMinor((long) +1000);

        BookTransactionRequest req = new BookTransactionRequest();
        req.setTxId(UUID.randomUUID());
        req.setEntries(List.of(c, d));

        service.bookTransaction(req);

        verify(ledgerRepo, times(2)).insert(any());
        verify(outboxRepo, atLeast(1)).insert(any());
        verifyNoMoreInteractions(ledgerRepo, outboxRepo);

    }
}
