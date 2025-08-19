package com.paystream.accountservice.unit;

import com.paystream.accountservice.api.AccountResponse;
import com.paystream.accountservice.api.CreateAccountRequest;
import com.paystream.accountservice.app.AccountAppService;
import com.paystream.accountservice.domain.Account;
import com.paystream.accountservice.domain.OutboxEvent;
import com.paystream.accountservice.infra.dao.AccountBalanceDao;
import com.paystream.accountservice.infra.dao.AccountDao;
import com.paystream.accountservice.infra.dao.OutboxDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// Pure unit test: no Spring context, just Mockito runner
@ExtendWith(MockitoExtension.class)
@Testcontainers(disabledWithoutDocker = true)
class AccountAppServiceTest {

    // Mock collaborators
    @Mock AccountDao accountDao;
    @Mock AccountBalanceDao balanceDao;
    @Mock OutboxDao outboxDao;

    // Inject mocks into the SUT (System Under Test)
    @InjectMocks AccountAppService service;

    @Test
    void createAccount_callsDaos_andReturnsResponse() {
        // Given a valid request
        var req = new CreateAccountRequest(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "TRY"
        );
        var headers = Map.of("x-trace-id", "u-1");

        // When the service is executed
        AccountResponse resp = service.createAccount(req, headers);

        // Then basic response fields are present
        assertThat(resp.currency()).isEqualTo("TRY");
        assertThat(resp.status()).isEqualTo("ACTIVE");
        assertThat(resp.id()).isNotNull();

        // And DAOs are called as expected
        verify(accountDao, times(1)).insert(any(Account.class));
        verify(balanceDao, times(1)).insertInitial(any(UUID.class));
        verify(outboxDao, times(2)).insert(any(OutboxEvent.class));
        verifyNoMoreInteractions(accountDao, balanceDao, outboxDao);
    }
}
