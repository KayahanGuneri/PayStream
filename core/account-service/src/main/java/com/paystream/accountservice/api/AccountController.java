package com.paystream.accountservice.api;


import com.paystream.accountservice.app.AccountAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountAppService service;

    @PostMapping
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest req,
                                  @RequestHeader Map<String, String> headers) {
        return service.createAccount(req, headers);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable UUID id) {
        return service.getBalance(id);
    }
}
