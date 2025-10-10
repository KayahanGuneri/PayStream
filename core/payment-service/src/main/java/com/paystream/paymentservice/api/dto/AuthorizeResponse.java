package com.paystream.paymentservice.api.dto;

//Minimal stub response; will evolve with providerRef/status/links
public record AuthorizeResponse (
        String paymentId,
        String status
){
        public static AuthorizeResponse stub(){
            return new AuthorizeResponse("stub-payment-id","AUTH_PENDING");
        }
}
