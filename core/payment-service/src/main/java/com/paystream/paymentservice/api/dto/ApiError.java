package com.paystream.paymentservice.api.dto;


// Lightweight error envelope for exception handlers
public record ApiError (
        String code,
        String message
){
}
