package com.paystream.transferservice.infra.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Writes FSM audit rows to transfer_steps (ISP: single table responsibility). */
@Repository
@RequiredArgsConstructor
public class TransferStepDao {

    private final NamedParameterJdbcTemplate jdbc;

    public void insert(UUID id, UUID transferId, String from, String to, String reason) {
        // Keep SQL focused; do not mix business decisions here.
        String sql = """
                INSERT INTO transfer_steps(id, transfer_id, from_state, to_state, reason)
                VALUES (:id, :tid, :from, :to, :reason)
                """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("tid", transferId)
                .addValue("from", from)
                .addValue("to", to)
                .addValue("reason", reason);
        jdbc.update(sql, p);
    }
}