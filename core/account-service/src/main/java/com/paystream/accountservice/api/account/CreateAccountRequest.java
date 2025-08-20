package com.paystream.accountservice.api.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** POST /v1/customers/{customerId}/accounts body */
public record CreateAccountRequest(
        @NotNull
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase 3-letter code (e.g., TRY)")
        String currency
) {}
