package com.paystream.transferservice.infra.dao;

import com.paystream.transferservice.domain.TransferStatus;
import com.paystream.transferservice.domain.TransferStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Adapter layer that persists FSM audit steps via JDBC DAO.
 * SRP: Only responsible for writing 'transfer_steps' audit rows.
 * DIP: App layer depends on TransferStepRepository, not on JDBC details.
 */
@Repository
@RequiredArgsConstructor
public class TransferStepRepositoryImpl implements TransferStepRepository {

    private final TransferStepDao transferStepDao;

    @Override
    public void save(UUID id, UUID transferId, TransferStatus from, TransferStatus to, String reason) {
        // Convert enums to DB string columns and delegate to low-level DAO
        // NOTE: Keep mapping here, do not leak JDBC to the app/domain layers.
        transferStepDao.insert(
                id,
                transferId,
                from.name(),   // store as TEXT in DB
                to.name(),     // store as TEXT in DB
                reason
        );
    }
}
