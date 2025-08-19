# Faz-1 — Account Service (DB, Outbox başlangıcı)

## 1) Veri Modeli & Transaction Sınırları
- **accounts**: (id [UUID PK], customer_id, currency[3], status, version, created_at, updated_at)
- **account_balances**: (account_id [UUID PK FK->accounts], current_balance NUMERIC(18,2), as_of_ledger_offset BIGINT, updated_at)
- **outbox_events**: (id, aggregate_type, aggregate_id, event_type, payload JSONB, headers JSONB, occurred_at, published_at)

**createAccount TX (single @Transactional):**
1. `accounts` insert (status=ACTIVE, version=0)
2. `account_balances` initial snapshot (current_balance=0.00)
3. `outbox_events` → `accounts.account-created.v1` ve `accounts.account-snapshot-initialized.v1`
> Hata olursa **hepsi rollback** (TransactionRollbackIT ile doğrulandı).

Balans okuma (`GET /accounts/{id}/balance`): **snapshot** (`account_balances`) üzerinden; ledger event’leri ilerleyen fazlarda güncellenecek (eventual consistency).

---

## 2) Outbox Pattern — Başlangıç
- **Neden?** DB yazımı ile event yayılımını ayırmak, fakat aynı **TX** içinde event’i güvenilir biçimde kaydetmek.
- **Nasıl?** İşlem içindeki domain değişikliğiyle **aynı transaction** içinde `outbox_events` tablosuna event satırı yazılır. Debezium Postgres CDC bu değişikliği okuyup Kafka’ya yazar.
- **Idempotency (plan):**
    - Faz-1: Uygulama düzeyinde idempotency uygulanmadı (yalnızca başlangıç).
    - Faz-2 planı:
        - `outbox_events` üzerinde **işlemsel benzersizlik** (ör. `(aggregate_id, event_type, occurred_at)` için unique index veya business key).
        - Tüketici tarafında **dedup**: event `id` bazında “already processed” set.
        - `x-trace-id` veya `commandId` ile **command idempotency** (unique constraint).
- **Hata senaryoları ve retry:**
    - DB yazıldı, Kafka düşmedi → Debezium CDC yakalar (en garantili yol).
    - Connector durakladı → geri geldikten sonra log’dan yakalar.
    - Tüketicide hata → offset ilerletme kontrolü ve retry.

---

## 3) Spring Data JDBC Notları
Bu projede **plain JDBC (JdbcTemplate)** kullanılıyor. Alternatif olarak **Spring Data JDBC** ile:
- `@Table`, `@Id`, `@Version` anotasyonlarıyla **optimistic locking** otomatikleşir.
- Repository interface’leri; entity state tracking olmadan “Aggregate as Root” yaklaşımı.
- **Doğru kullanım:**
    - Basit Aggregate yapıları, lazy/complex relations yoksa **Spring Data JDBC** idealdir.
    - `@Version` alanı (ör. `long version`) ile update sırasında `WHERE version=?` kullanımı framework tarafından yapılır.
- **Yanlış/kaçınılması gereken:**
    - Çok karmaşık ilişkiler, lazy loading beklentisi.
    - JPA özellikleri (EntityManager, JPQL) beklemek — Spring Data JDBC bunları sağlamaz.

**@Version örneği (ileride geçmek isterseniz):**
```java
// Example entity for Spring Data JDBC
@Table("accounts")
public class AccountEntity {
  @Id
  UUID id;

  String currency;
  String status;

  @Version
  Long version; // Optimistic locking field
}
