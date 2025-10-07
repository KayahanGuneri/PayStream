package com.paystream.transferservice.infra.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.UUID;

@Repository
public class OutboxDao {

    private final JdbcTemplate jdbc;

    public OutboxDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    /**
     * outbox_events tablosuna satır ekler.
     * Şema: aggregate_type(text), aggregate_id(uuid), key_account_id(uuid), payload_json(jsonb), status(text)
     * Not: JSONB için PGobject vs. gerek yok; '?' parametresini "::jsonb" ile cast ediyoruz.
     */
    public void append(String aggregateType,
                       UUID aggregateId,
                       UUID keyAccountId,
                       String payloadJson) {

        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    """
                    INSERT INTO outbox_events(aggregate_type, aggregate_id, key_account_id, payload_json, status)
                    VALUES (?, ?, ?, (?::jsonb), 'NEW')
                    """
            );
            ps.setString(1, aggregateType);
            ps.setObject(2, aggregateId, Types.OTHER);     // PG uuid
            if (keyAccountId == null) {
                ps.setNull(3, Types.OTHER);
            } else {
                ps.setObject(3, keyAccountId, Types.OTHER); // PG uuid
            }
            ps.setString(4, payloadJson);                   // ::jsonb ile cast edeceğiz
            return ps;
        });

    public void append(String aggregateType, UUID aggregateId, UUID keyAccountId, String payloadJson) {
        jdbc.update("""
            INSERT INTO account.outbox_events
                (aggregate_type, aggregate_id, key_account_id, payload, status)
            VALUES (?, ?, ?, ?::jsonb, 'NEW')
        """, aggregateType, aggregateId, keyAccountId, payloadJson);

    }
}
