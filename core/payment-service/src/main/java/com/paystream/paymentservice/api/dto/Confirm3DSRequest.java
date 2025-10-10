package com.paystream.paymentservice.api.dto;

import java.math.BigDecimal;


// For confirming 3DS challenge later via Redis session
public record Confirm3DSRequest (
        BigDecimal amount
){ }
