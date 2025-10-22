package com.paystream.paymentservice.api.dto;

import java.math.BigDecimal;


public record CaptureRequest(
        BigDecimal amount
) {}
