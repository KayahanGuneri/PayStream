package com.paystream.transferservice;

import com.paystream.transferservice.api.TransferResponse;
import com.paystream.transferservice.api.mapper.TransferApiMapper;
import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Purpose: Verify static mapping from Domain -> API DTO.
 * Why: Utility class has private ctor; methods are static and stateless.
 */
class TransferApiMapperTest {

    @Test
    void should_map_domain_transfer_to_api_response() {
        // given: a domain Transfer filled with typical values
        Transfer t = new Transfer();
        UUID id = UUID.randomUUID();
        t.id = id;
        t.status = TransferStatus.COMPLETED;
        t.ledgerTxId = UUID.randomUUID();

        // when: map using static utility method
        TransferResponse resp = TransferApiMapper.toResponse(t);

        // then: response mirrors domain fields the client needs
        assertThat(resp.transferId()).isEqualTo(id);
        assertThat(resp.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(resp.ledgerTxId()).isEqualTo(t.ledgerTxId);
    }

    @Test
    void should_allow_null_ledgerTxId_in_response() {
        // given
        Transfer t = new Transfer();
        t.id = UUID.randomUUID();
        t.status = TransferStatus.FAILED;
        t.ledgerTxId = null; // failed flows may not have a ledger tx id

        // when
        TransferResponse resp = TransferApiMapper.toResponse(t);

        // then
        assertThat(resp.ledgerTxId()).isNull();
    }
}
