package com.paystream.accountservice.app.customer;

import com.paystream.accountservice.api.customer.CustomerResponse;
import com.paystream.accountservice.domain.customer.Customer;
import com.paystream.accountservice.domain.outbox.OutboxEvent;
import com.paystream.accountservice.infra.dao.customer.CustomerDao;
import com.paystream.accountservice.infra.dao.outbox.OutboxDao;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Handles customer registration and retrieval.
 * Emits an outbox event on registration (if needed by other services).
 */
@Service
public class CustomerAppService {

    private final CustomerDao customerDao;
    private final OutboxDao outboxDao;
    private final PasswordEncoder passwordEncoder;

    public CustomerAppService(CustomerDao customerDao, OutboxDao outboxDao, PasswordEncoder passwordEncoder) {
        this.customerDao = customerDao;
        this.outboxDao = outboxDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerResponse register(String name, String email, String rawPassword) {
        if (customerDao.existsByEmail(email)) {
            // Consider mapping to 409 via @ControllerAdvice.
            throw new IllegalStateException("Email already registered");
        }

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        String hash = passwordEncoder.encode(rawPassword);

        customerDao.insert(new Customer(id, name, email, hash, now, now));

        // Optional outbox event for downstream consumers (CRM, analytics, etc.)
        outboxDao.insert(new OutboxEvent(
                UUID.randomUUID(),
                "CUSTOMER",
                id,
                "customers.customer-created.v1",
                Map.of("customerId", id.toString(), "email", email, "name", name),
                Map.of(),
                now,
                null
        ));

        return new CustomerResponse(id, name, email);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        var c = customerDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return new CustomerResponse(c.id(), c.name(), c.email());
    }
}
