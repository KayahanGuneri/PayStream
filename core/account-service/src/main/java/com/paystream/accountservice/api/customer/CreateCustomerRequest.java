package com.paystream.accountservice.api.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for customer registration.
 * The raw password is hashed server-side.
 */
public record CreateCustomerRequest(
        @NotBlank @Size(max = 128) String name,
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
