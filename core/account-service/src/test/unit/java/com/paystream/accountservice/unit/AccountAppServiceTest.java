package com.paystream.accountservice.unit;


import com.paystream.accountservice.api.account.AccountResponse;
import com.paystream.accountservice.app.account.AccountAppService;
import com.paystream.accountservice.domain.account.Account;
import com.paystream.accountservice.domain.outbox.OutboxEvent;
import com.paystream.accountservice.infra.dao.account.AccountBalanceDao;
import com.paystream.accountservice.infra.dao.account.AccountDao;
import com.paystream.accountservice.infra.dao.outbox.OutboxDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for AccountAppService:
 * - No Spring context
 * - All collaborators are mocked
 *
 * Contract under test:
 *   AccountAppService.openFor(UUID customerId, String currency)
 */
@ExtendWith(MockitoExtension.class)
class AccountAppServiceTest {

    // --- Mock collaborators ---
    @Mock AccountDao accountDao;
    @Mock AccountBalanceDao balanceDao;
    @Mock OutboxDao outboxDao;

    // --- System under test ---
    @InjectMocks AccountAppService service;

    @Test
    @DisplayName("openFor: persists account, inserts initial balance, writes outbox with correct payload")
    void openFor_shouldPersistDomain_InsertInitialBalance_andWriteOutbox() {
        // Given
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String currency = "TRY";

        // When
        AccountResponse resp = service.openFor(customerId, currency);

        // Then: response basics
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isNotNull();
        assertThat(resp.currency()).isEqualTo("TRY");
        assertThat(resp.status()).isEqualTo("ACTIVE");

        // Domain write
        verify(accountDao, times(1)).insert(any(Account.class));

        // Initial balance for the created account id
        ArgumentCaptor<UUID> accIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(balanceDao, times(1)).insertInitial(accIdCaptor.capture());
        assertThat(accIdCaptor.getValue()).isEqualTo(resp.id());

        // Exactly one outbox event with expected content
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxDao, times(1)).insert(eventCaptor.capture());
        OutboxEvent evt = eventCaptor.getValue();

        assertThat(evt).isNotNull();
        assertThat(evt.aggregateId()).isEqualTo(resp.id());
        assertThat(evt.aggregateType()).isEqualTo("ACCOUNT");
        assertThat(evt.eventType()).isEqualTo("accounts.account-created.v1");
        assertThat(evt.payload())
                .containsEntry("accountId", resp.id().toString())
                .containsEntry("customerId", customerId.toString())
                .containsEntry("currency", currency)
                .containsEntry("status", "ACTIVE");

        verifyNoMoreInteractions(accountDao, balanceDao, outboxDao);
    }

    @Test
    @DisplayName("openFor: calls DAOs as expected and returns a valid response")
    void openFor_withValidInput_callsDaos_andReturnsResponse() {
        // Given
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String currency = "TRY";

        // When
        AccountResponse resp = service.openFor(customerId, currency);

        // Then
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isNotNull();
        assertThat(resp.currency()).isEqualTo("TRY");
        assertThat(resp.status()).isEqualTo("ACTIVE");

        verify(accountDao, times(1)).insert(any(Account.class));
        verify(balanceDao, times(1)).insertInitial(any(UUID.class));
        verify(outboxDao, times(1)).insert(any(OutboxEvent.class));

        verifyNoMoreInteractions(accountDao, balanceDao, outboxDao);
    }
}
