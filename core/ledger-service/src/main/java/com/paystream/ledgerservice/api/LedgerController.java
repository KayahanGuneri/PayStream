package com.paystream.ledgerservice.api;

import com.paystream.ledgerservice.app.LedgerTransactionAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerTransactionAppService app;

    // Internal-only endpoint to book a transaction containing N entries
    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void book(@Valid @RequestBody BookTransactionRequest req) {
        // Delegate to the application service; transactional boundary is there
        app.bookTransaction(req);
    }
}
