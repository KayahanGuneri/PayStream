package com.paystream.accountservice.app;

import com.paystream.accountservice.api.AccountResponse;
import com.paystream.accountservice.api.BalanceResponse;
import com.paystream.accountservice.api.CreateAccountRequest;
import com.paystream.accountservice.domain.Account;
import com.paystream.accountservice.domain.OutboxEvent;
import com.paystream.accountservice.infra.dao.AccountBalanceDao;
import com.paystream.accountservice.infra.dao.AccountDao;
import com.paystream.accountservice.infra.dao.OutboxDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class AccountAppService {

    private final AccountDao accountDao;
    private final AccountBalanceDao balanceDao;
    private final OutboxDao outboxDao;

    public AccountAppService(AccountDao accountDao, AccountBalanceDao balanceDao, OutboxDao outboxDao) {
        this.accountDao = accountDao;
        this.balanceDao = balanceDao;
        this.outboxDao = outboxDao;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest req, Map<String,String> headers) {
        UUID id = UUID.randomUUID();

        // 1) accounts insert
        Account acc = new Account(
                id, req.customerId(), req.currency(), "ACTIVE", 0L, Instant.now(), Instant.now()
        );
        accountDao.insert(acc);

        // 2) account_balances initial snapshot
        balanceDao.insertInitial(id);

        // 3) outbox events (aynı TX içinde)
        var createdPayload = Map.<String,Object>of(
                "accountId", id.toString(),
                "customerId", String.valueOf(req.customerId()),
                "currency", req.currency(),
                "status", "ACTIVE"
        );
        outboxDao.insert(new OutboxEvent(
                UUID.randomUUID(), "Account", id, "accounts.account-created.v1",
                createdPayload, headers, Instant.now()
        ));

        var snapPayload = Map.<String,Object>of(
                "accountId", id.toString(),
                "currentBalance", "0.00"
        );
        outboxDao.insert(new OutboxEvent(
                UUID.randomUUID(), "Account", id, "accounts.account-snapshot-initialized.v1",
                snapPayload, headers, Instant.now()
        ));

        return new AccountResponse(id, req.currency(), "ACTIVE");
    }

    public BalanceResponse getBalance(UUID accountId) {
        var bal = balanceDao.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("account not found"));
        return new BalanceResponse(bal.accountId(), bal.currentBalance(), bal.asOfLedgerOffset(), bal.updatedAt());
    }
}
