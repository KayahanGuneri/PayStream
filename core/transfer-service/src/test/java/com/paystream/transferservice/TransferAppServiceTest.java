package com.paystream.transferservice;

import com.paystream.transferservice.api.CreateTransferRequest;
import com.paystream.transferservice.app.TransferAppService;
import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import com.paystream.transferservice.infra.client.LedgerClient;
import com.paystream.transferservice.infra.dao.OutboxDao;
import com.paystream.transferservice.infra.dao.TransferDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferAppServiceTest {

    @Mock TransferDao transferDao;
    @Mock LedgerClient ledgerClient;
    @Mock OutboxDao outboxDao;

    @InjectMocks TransferAppService service;

    private CreateTransferRequest req;

    @BeforeEach
    void setUp() {
        req = new CreateTransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TRY",
                1_000L
        );
    }

    @Test
    @DisplayName("Happy-path: PENDING -> IN_PROGRESS -> COMPLETED, outbox TRANSFER_COMPLETED")
    void should_complete_when_ledger_succeeds() {
        when(transferDao.findByIdempotencyKey("KEY-1")).thenReturn(Optional.empty());

        // insertPending çağrısında statünün PENDING olduğunu burada assert ediyoruz
        doNothing().when(transferDao).insertPending(
                argThat(t -> t != null && t.status == TransferStatus.PENDING)
        );

        doNothing().when(transferDao).updateStatus(any(UUID.class), eq(TransferStatus.IN_PROGRESS));

        when(ledgerClient.appendDoubleEntry(any(UUID.class), any(UUID.class), any(UUID.class),
                anyString(), anyLong())).thenReturn(true);

        doNothing().when(transferDao).markCompleted(any(UUID.class), any(UUID.class));
        doNothing().when(outboxDao).append(eq("TRANSFER_COMPLETED"), any(UUID.class), any(UUID.class), anyString());

        Transfer result = service.createTransfer("KEY-1", req);

        verify(transferDao).insertPending(any(Transfer.class));
        verify(transferDao).updateStatus(any(UUID.class), eq(TransferStatus.IN_PROGRESS));
        verify(transferDao).markCompleted(any(UUID.class), any(UUID.class));
        verify(outboxDao).append(eq("TRANSFER_COMPLETED"), any(UUID.class), eq(req.destAccountId()), anyString());

        assertThat(result.status).isEqualTo(TransferStatus.COMPLETED);
        assertThat(result.ledgerTxId).isNotNull();
    }

    @Test
    @DisplayName("Ledger failure: PENDING -> IN_PROGRESS -> FAILED, outbox TRANSFER_FAILED")
    void should_fail_when_ledger_fails() {
        when(transferDao.findByIdempotencyKey("KEY-2")).thenReturn(Optional.empty());

        doNothing().when(transferDao).insertPending(
                argThat(t -> t != null && t.status == TransferStatus.PENDING)
        );
        doNothing().when(transferDao).updateStatus(any(UUID.class), eq(TransferStatus.IN_PROGRESS));

        when(ledgerClient.appendDoubleEntry(any(UUID.class), any(UUID.class), any(UUID.class),
                anyString(), anyLong())).thenReturn(false);

        doNothing().when(transferDao).markFailed(any(UUID.class));
        doNothing().when(outboxDao).append(eq("TRANSFER_FAILED"), any(UUID.class), isNull(), anyString());

        Transfer result = service.createTransfer("KEY-2", req);

        assertThat(result.status).isEqualTo(TransferStatus.FAILED);
        verify(transferDao).markFailed(any(UUID.class));
        verify(outboxDao).append(eq("TRANSFER_FAILED"), any(UUID.class), isNull(), anyString());
    }

    @Test
    @DisplayName("Idempotency: mevcut transfer döner, yeni DB/ledger çağrısı olmaz")
    void should_return_existing_on_idempotency() {
        var existing = new Transfer();
        existing.id = UUID.randomUUID();
        existing.status = TransferStatus.COMPLETED;
        when(transferDao.findByIdempotencyKey("KEY-3")).thenReturn(Optional.of(existing));

        Transfer result = service.createTransfer("KEY-3", req);

        assertThat(result).isSameAs(existing);
        verifyNoInteractions(ledgerClient, outboxDao);
        verify(transferDao, never()).insertPending(any());
        verify(transferDao, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("Guard: source == dest -> anında FAILED, DB/ledger/outbox yok")
    void should_guard_same_source_and_dest() {
        UUID same = UUID.randomUUID();
        var badReq = new CreateTransferRequest(same, same, "TRY", 100L);
        when(transferDao.findByIdempotencyKey("KEY-4")).thenReturn(Optional.empty());

        Transfer result = service.createTransfer("KEY-4", badReq);

        assertThat(result.status).isEqualTo(TransferStatus.FAILED);
        verify(transferDao, never()).insertPending(any());
        verify(transferDao, never()).updateStatus(any(), any());
        verifyNoInteractions(ledgerClient, outboxDao);
    }
}
