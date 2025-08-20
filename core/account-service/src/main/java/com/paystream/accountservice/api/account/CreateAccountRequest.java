package com.paystream.accountservice.api.account;// package your.package.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for opening a new account.
 * NOTE:
 * - We do NOT accept customerId in the body anymore.
 * - The customerId will be provided by the PATH parameter or derived from auth.
 */
public record CreateAccountRequest(
        java.util.UUID uuid, @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be ISO 4217 uppercase 3-letter code (e.g., TRY, USD, EUR)")
        String currency
) {}
