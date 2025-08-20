package com.paystream.accountservice.api.customer;

import java.util.UUID;

/**
 * Public customer view. Never exposes password hashes.
 */
public record CustomerResponse(
        UUID id,
        String name,
        String email
) {}
