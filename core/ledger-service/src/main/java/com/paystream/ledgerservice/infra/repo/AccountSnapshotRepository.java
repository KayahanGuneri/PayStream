package com.paystream.ledgerservice.infra.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Account + currency başına snapshot tutar.
 * Idempotency: aynı veya eski ledger_offset geldiyse NO-OP.
 */
@Repository
@RequiredArgsConstructor
public class AccountSnapshotRepository {

    private final JdbcTemplate jdbc;

    /**

     * @param accountId    hesap ID
     * @param currency     para birimi
     * @param deltaMinor   eklenecek/çıkarılacak tutar (minor units)
     * @param ledgerOffset global monotonik offset (olay sırası)
     *
     * Eğer row yoksa INSERT, varsa yalnızca newOffset > existingOffset ise UPDATE yapılır.
     */
    public void applyDelta(UUID accountId, String currency, long deltaMinor, long ledgerOffset) {
        final String sql = """
<<<<<<< HEAD
                INSERT INTO account_snapshots(account_id, currency, balance_minor, as_of_ledger_offset)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (account_id, currency)
                DO UPDATE SET
                    balance_minor       = account_snapshots.balance_minor + EXCLUDED.balance_minor,
                    as_of_ledger_offset = EXCLUDED.as_of_ledger_offset
                WHERE account_snapshots.as_of_ledger_offset IS NULL
                   OR account_snapshots.as_of_ledger_offset < EXCLUDED.as_of_ledger_offset
                """;
=======
        INSERT INTO account_snapshots(account_id, currency, balance_minor, as_of_ledger_offset)
        VALUES (?, ?, ?, ?)
        ON CONFLICT (account_id, currency)
        DO UPDATE SET
            balance_minor       = account_snapshots.balance_minor + EXCLUDED.balance_minor,
            as_of_ledger_offset = EXCLUDED.as_of_ledger_offset
        WHERE account_snapshots.as_of_ledger_offset IS NULL
           OR account_snapshots.as_of_ledger_offset < EXCLUDED.as_of_ledger_offset
        """;
        jdbc.update(sql, accountId, currency, deltaMinor, ledgerOffset); // <- BUNUN VAR OLMASI ŞART
>>>>>>> feat/w5-transfer-repro
    }


}
