package com.paystream.transferservice;

import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import com.paystream.transferservice.infra.mapper.TransferRowMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Purpose: Verify ResultSet -> Domain mapping logic for all columns.
 * Why: Column names and nullability errors are common in JDBC code.
 */
class TransferRowMapperTest {

    @Test
    void should_map_all_columns_from_resultset() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        UUID id = UUID.randomUUID();
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        Instant now = Instant.now();

        when(rs.getString("id")).thenReturn(id.toString());
        when(rs.getString("source_account_id")).thenReturn(from.toString());
        when(rs.getString("dest_account_id")).thenReturn(to.toString());
        when(rs.getString("currency")).thenReturn("EUR");
        when(rs.getLong("amount_minor")).thenReturn(9999L);
        when(rs.getString("idempotency_key")).thenReturn("IDEMP-999");
        when(rs.getString("status")).thenReturn("IN_PROGRESS");
        when(rs.getString("ledger_tx_id")).thenReturn(UUID.randomUUID().toString());
        when(rs.getObject("created_at", java.time.OffsetDateTime.class)).thenReturn(java.time.OffsetDateTime.now());
        when(rs.getObject("updated_at", java.time.OffsetDateTime.class)).thenReturn(java.time.OffsetDateTime.now());

        TransferRowMapper mapper = new TransferRowMapper();

        Transfer t = mapper.mapRow(rs, 0);

        assertThat(t.id).isEqualTo(id);
        assertThat(t.sourceAccountId).isEqualTo(from);
        assertThat(t.destAccountId).isEqualTo(to);
        assertThat(t.currency).isEqualTo("EUR");
        assertThat(t.amountMinor).isEqualTo(9999L);
        assertThat(t.idempotencyKey).isEqualTo("IDEMP-999");
        assertThat(t.status).isEqualTo(TransferStatus.IN_PROGRESS);
        assertThat(t.ledgerTxId).isNotNull();
        assertThat(t.createdAt).isNotNull();
        assertThat(t.updatedAt).isNotNull();
    }

    @Test
    void should_handle_nullable_ledger_tx_id() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("id")).thenReturn(UUID.randomUUID().toString());
        when(rs.getString("source_account_id")).thenReturn(UUID.randomUUID().toString());
        when(rs.getString("dest_account_id")).thenReturn(UUID.randomUUID().toString());
        when(rs.getString("currency")).thenReturn("TRY");
        when(rs.getLong("amount_minor")).thenReturn(100L);
        when(rs.getString("idempotency_key")).thenReturn("I-1");
        when(rs.getString("status")).thenReturn("PENDING");
        when(rs.getString("ledger_tx_id")).thenReturn(null); // ← nullable
        when(rs.getObject("created_at", java.time.OffsetDateTime.class)).thenReturn(java.time.OffsetDateTime.now());
        when(rs.getObject("updated_at", java.time.OffsetDateTime.class)).thenReturn(java.time.OffsetDateTime.now());

        TransferRowMapper mapper = new TransferRowMapper();

        Transfer t = mapper.mapRow(rs, 0);

        assertThat(t.ledgerTxId).isNull();
    }
}
