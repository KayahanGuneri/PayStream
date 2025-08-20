package com.paystream.accountservice.domain.customer;

import java.time.Instant;
import java.util.UUID;

/**
 * Customer aggregate (identity profile).
 * NOTE: Only passwordHash is stored; never keep raw passwords.
 */
public record Customer(
        UUID id,
        String name,
        String email,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt
) {}
