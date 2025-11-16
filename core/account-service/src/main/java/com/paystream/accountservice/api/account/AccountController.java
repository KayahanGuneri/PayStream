package com.paystream.accountservice.api.account;

import com.paystream.accountservice.app.account.AccountAppService;
import com.paystream.accountservice.infra.dao.account.AccountDao;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Account HTTP endpoints.
 */
@RestController
@RequestMapping("/v1")
public class AccountController {

    private final AccountAppService accountAppService;
    private final AccountDao accountDao;

    public AccountController(AccountAppService accountAppService, AccountDao accountDao) {
        this.accountAppService = accountAppService;
        this.accountDao = accountDao;
    }

    /**
     * Hesap açma
     * POST /v1/customers/{customerId}/accounts
     */
    @PostMapping("/customers/{customerId}/accounts")
    public ResponseEntity<AccountResponse> open(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateAccountRequest req
    ) {
        var resp = accountAppService.openFor(customerId, req.currency());
        var location = URI.create("/v1/accounts/" + resp.id());
        return ResponseEntity.created(location).body(resp);
    }

    /**
     * Tekil hesap getirme
     * GET /v1/accounts/{accountId}
     */
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        var acc = accountDao.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        var resp = new AccountResponse(acc.id(), acc.currency(), acc.status());
        return ResponseEntity.ok(resp);
    }

    /**
     * Belirli bir müşteriye ait TÜM hesapları listeleme
     * GET /v1/customers/{customerId}/accounts
     *
     * FE: useAccountsList → GET /api/v1/customers/{customerId}/accounts
     */
    @GetMapping("/customers/{customerId}/accounts")
    public ResponseEntity<List<AccountResponse>> listByCustomer(@PathVariable UUID customerId) {
        var accounts = accountDao.findByCustomerId(customerId);

        var responses = accounts.stream()
                .map(a -> new AccountResponse(a.id(), a.currency(), a.status()))
                .toList();

        // Boşsa bile 200 + [] döner → FE tarafında 500 yerine boş liste görürsün
        return ResponseEntity.ok(responses);
    }
}
