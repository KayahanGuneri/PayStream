package com.paystream.accountservice.api.account;

import com.paystream.accountservice.app.account.AccountAppService;
import com.paystream.accountservice.infra.dao.account.AccountDao;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping("/customers/{customerId}/accounts")
    public ResponseEntity<AccountResponse> openForCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateAccountRequest req
    ) {
        var resp = accountAppService.openFor(customerId, req.currency());
        return ResponseEntity.created(URI.create("/v1/accounts/" + resp.id())).body(resp);
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        var acc = accountDao.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return ResponseEntity.ok(new AccountResponse(acc.id(), acc.currency(), acc.status()));
    }
}
