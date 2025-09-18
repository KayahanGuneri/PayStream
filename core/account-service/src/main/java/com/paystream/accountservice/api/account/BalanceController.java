package com.paystream.accountservice.api.account;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/** Read-only balance endpoint for smoke tests. */
@RestController
@RequestMapping("/v1")
public class BalanceController {

    private final JdbcTemplate jdbc;
    public BalanceController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/accounts/{accountId}/balance")
    public Map<String, Object> get(@PathVariable UUID accountId) {
        return jdbc.queryForMap("""
            SELECT account_id, balance_minor, as_of_ledger_offset, updated_at
              FROM account_balances WHERE account_id = ?
        """, accountId);
    }
}
