package com.paystream.paymentservice.api.dto;


//Refund can be full or partial;rules enforced in app layer
public record RefundRequest (
        String code,
        String message
){ }
