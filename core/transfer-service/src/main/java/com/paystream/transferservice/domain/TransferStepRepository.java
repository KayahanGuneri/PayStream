package com.paystream.transferservice.domain;

import java.util.UUID;

/** Abstraction for persisting FSM audit steps (keeps app layer decoupled from JDBC). */
public interface TransferStepRepository {
    void save(UUID id, UUID transferId, TransferStatus from, TransferStatus to, String reason);
}
