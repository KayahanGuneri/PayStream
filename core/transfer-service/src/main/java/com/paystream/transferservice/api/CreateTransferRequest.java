package com.paystream.transferservice.api;

//API DTO for creating a transfer request
//Records are immutable data carriers in Java; great for request/response bodies.


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateTransferRequest (

    @NotNull
    UUID sourceAccountId,   //Must be valid UUID

    @NotNull
    UUID destACcountId,    //Must be valid UUID

    @NotBlank
    String currency,       //e.g. "TRY"

    @Positive
    long amountMinor      //positive amount in minor units
){}
