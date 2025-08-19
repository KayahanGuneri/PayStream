package com.paystream.accountservice.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

// Enforce currency to be exactly 3 uppercase letters (e.g., "TRY")
public record CreateAccountRequest(
        @NotNull UUID customerId,
        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be 3 uppercase letters (e.g., TRY)")
        String currency
) {}
