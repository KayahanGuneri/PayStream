package com.paystream.paymentservice.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AuthorizeRequest(
        @NotBlank String merchantId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency, // ISO-4217 (TRY, USD, EUR...)
        @NotBlank String cardToken
) {}
