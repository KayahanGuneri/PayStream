package com.paystream.ledgerservice.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.ledgerservice.api.BookTransactionRequest;
import com.paystream.ledgerservice.domain.LedgerEntry;
import com.paystream.ledgerservice.domain.OutboxRecord;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository;
import com.paystream.ledgerservice.infra.repo.OutboxRepository;
// import jakarta.validation.ValidationException; // <-- KALDIRILDI
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerTransactionAppService {

    private final LedgerEntryRepository entryRepo;
    private final OutboxRepository outboxRepo;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional
    public void bookTransaction(BookTransactionRequest req) {
        // --- 1) Keeping business rules in order ---
        validate(req);

        // --- 2) Append records (append-only) and sum the given offsets ---
        int seq = 0;
        // ...
        for (BookTransactionRequest.Entry line : req.getEntries()) {
            LedgerEntry e = LedgerEntry.builder()
                    .entryId(UUID.randomUUID())
                    .txId(req.getTxId())
                    .txSeq(seq++)
                    .accountId(line.getAccountId())
                    .currency(line.getCurrency())
                    .amountMinor(line.getAmountMinor())
                    .build();

            var result = entryRepo.upsert(e); // <-- insert yerine upsert kullan

            if (!result.inserted()) {
                // zaten vardı → outbox YAZMA (idempotency)
                continue;
            }

            // --- outbox sadece yeni kayıt için ---
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("txId", e.getTxId().toString());
            payload.put("entryId", e.getEntryId().toString());
            payload.put("ledgerOffset", e.getLedgerOffset());
            payload.put("accountId", e.getAccountId().toString());
            payload.put("currency", e.getCurrency());
            payload.put("amountMinor", e.getAmountMinor());
            payload.put("occurredAt", Date.from(e.getCreatedAt().toInstant()));

            try {
                outboxRepo.insert(OutboxRecord.builder()
                        .id(UUID.randomUUID())
                        .eventType("ledger.entry.appended")
                        .keyAccountId(e.getAccountId())
                        .payload(om.writeValueAsString(payload))
                        .build());
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Failed to serialize event payload", ex);
            }
        }


    }

    // Basic invariants: at least one row, single currency, non-0 amounts, total = 0
    private void validate(BookTransactionRequest req) {
        if (req == null || req.getEntries() == null || req.getEntries().isEmpty()) {
            throw new IllegalArgumentException("At least one entry is required");
        }

        // All currency fields must be the same (single currency transaction)
        String currency = req.getEntries().get(0).getCurrency();
        boolean sameCurrency = req.getEntries().stream()
                .allMatch(e -> Objects.equals(currency, e.getCurrency()));
        if (!sameCurrency) {
            throw new IllegalArgumentException("All entries must have the same currency");
        }

        // No amount should be 0
        boolean nonZero = req.getEntries().stream().allMatch(e -> e.getAmountMinor() != 0);
        if (!nonZero) {
            throw new IllegalArgumentException("Entry amount must be non-zero");
        }

        // Double-entry accounting: signed sum must be 0
        long sum = req.getEntries().stream()
                .collect(Collectors.summingLong(BookTransactionRequest.Entry::getAmountMinor));
        if (sum != 0) {
            throw new IllegalArgumentException("Signed amounts must sum to zero");
        }
    }
}
