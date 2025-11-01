package com.paystream.ledgerservice.unit;

import com.paystream.ledgerservice.app.LedgerTransactionAppService;
import com.paystream.ledgerservice.api.BookTransactionRequest;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository;
import com.paystream.ledgerservice.infra.repo.OutboxRepository;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository.InsertResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerTransactionAppServiceValidationTest {

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

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.bookTransaction(req));

        assertTrue(ex.getMessage().toLowerCase().contains("entry"));
        verifyNoInteractions(ledgerRepo, outboxRepo);
    }

    @Test
    void mixedCurrencies_shouldThrow() {
        BookTransactionRequest.Entry e1 = new BookTransactionRequest.Entry();
        e1.setAccountId(UUID.randomUUID());
        e1.setCurrency("TRY");
        e1.setAmountMinor(-1000L);

        BookTransactionRequest.Entry e2 = new BookTransactionRequest.Entry();
        e2.setAccountId(UUID.randomUUID());
        e2.setCurrency("USD");
        e2.setAmountMinor(+1000L);

        BookTransactionRequest req = new BookTransactionRequest();
        req.setTxId(UUID.randomUUID());
        req.setEntries(List.of(e1, e2));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.bookTransaction(req));

        assertTrue(ex.getMessage().toLowerCase().contains("currency"));
        verifyNoInteractions(ledgerRepo, outboxRepo);
    }

    @Test
    void sumNotZero_shouldThrow() {
        BookTransactionRequest.Entry only = new BookTransactionRequest.Entry();
        only.setAccountId(UUID.randomUUID());
        only.setCurrency("TRY");
        only.setAmountMinor(+1000L); // tek taraflı → toplam 0 değil

        BookTransactionRequest req = new BookTransactionRequest();
        req.setTxId(UUID.randomUUID());
        req.setEntries(List.of(only));

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.bookTransaction(req));

        assertTrue(ex.getMessage().toLowerCase().contains("sum"));
        verifyNoInteractions(ledgerRepo, outboxRepo);
    }

    @Test
    void happyPath_shouldCallRepos() {
        // two-legged, aynı para birimi ve toplam 0
        BookTransactionRequest.Entry c = new BookTransactionRequest.Entry();
        c.setAccountId(UUID.randomUUID());
        c.setCurrency("TRY");
        c.setAmountMinor(-1000L);

        BookTransactionRequest.Entry d = new BookTransactionRequest.Entry();
        d.setAccountId(UUID.randomUUID());
        d.setCurrency("TRY");
        d.setAmountMinor(+1000L);

        BookTransactionRequest req = new BookTransactionRequest();
        req.setTxId(UUID.randomUUID());
        req.setEntries(List.of(c, d));

        // Servis upsert dönüşünü kullanıyorsa NPE olmasın diye dummy InsertResult dön.
        InsertResult dummy = mock(InsertResult.class, RETURNS_DEFAULTS);
        // Eğer InsertResult içinde offset accessor'ı varsa (ör. offset()/getOffset()),
        // aşağıdakini aktif edebilirsin:
        // when(dummy.offset()).thenReturn(1L);

        when(ledgerRepo.upsert(any())).thenReturn(dummy);

        service.bookTransaction(req);

        // Ledger'a iki kayıt (debit/credit) yazılmalı
        verify(ledgerRepo, times(2)).upsert(any());

        // Outbox'a en az bir kayıt yazılmalı (tek event ya da entry başına olabilir)
        verify(outboxRepo, atLeastOnce()).insert(any());
    }
}
