package com.paystream.accountservice.app.account;

import com.paystream.accountservice.api.account.AccountResponse;
import com.paystream.accountservice.domain.account.Account;
import com.paystream.accountservice.domain.outbox.OutboxEvent;
import com.paystream.accountservice.infra.dao.account.AccountBalanceDao;
import com.paystream.accountservice.infra.dao.account.AccountDao;
import com.paystream.accountservice.infra.dao.outbox.OutboxDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates account opening in a single transaction:
 * 1) Insert account (server-side UUID)
 * 2) Insert initial balance
 * 3) Insert outbox event(s)
 *
 * Guarantees:
 * - Domain write and outbox write are atomic.
 */
@Service
public class AccountAppService {

    private final AccountDao accountDao;
    private final AccountBalanceDao accountBalanceDao;
    private final OutboxDao outboxDao;

    public AccountAppService(AccountDao accountDao,
                             AccountBalanceDao accountBalanceDao,
                             OutboxDao outboxDao) {
        this.accountDao = accountDao;
        this.accountBalanceDao = accountBalanceDao;
        this.outboxDao = outboxDao;
    }

    /**
     * Open a new account for an existing customer.
     * @param customerId The existing customer's id (from PATH or auth)
     * @param currency   ISO 4217 code (e.g., TRY)
     */
    @Transactional
    public AccountResponse openFor(UUID customerId, String currency) {
        // Generate server-side UUID for the account
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();

        // 1) Insert domain aggregate (version starts from 0; status ACTIVE)
        Account account = new Account(
                accountId,
                customerId,
                currency,
                "ACTIVE", // tip: consider enum + DB CHECK constraint
                0L,       // optimistic lock version
                now,
                now
        );
        accountDao.insert(account);

        // 2) Insert initial balance row (0 amount, null offset)
        accountBalanceDao.insertInitial(accountId);

        // 3) Record outbox event for "account created"
        outboxDao.insert(new OutboxEvent(
                UUID.randomUUID(),             // outbox event id
                "ACCOUNT",                     // aggregate type
                accountId,                     // aggregate id
                "accounts.account-created.v1", // versioned event name
                Map.of(                        // minimal payload
                        "accountId", accountId.toString(),
                        "customerId", customerId.toString(),
                        "currency", currency,
                        "status", "ACTIVE"
                ),
                Map.of(),                      // headers (traceId/correlationId) if available
                now,
                null                           // published_at is NULL; publisher will set it
        ));

        // Map domain to response DTO
        return new AccountResponse(accountId, currency, "ACTIVE");
    }
}
