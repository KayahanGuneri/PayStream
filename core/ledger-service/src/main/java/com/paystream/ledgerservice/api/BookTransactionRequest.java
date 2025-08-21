package com.paystream.ledgerservice.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookTransactionRequest {

    //Correlation id for this business transaction
    @NotNull
    private UUID txId;

    //Lines to post; amounts are signed
    @NotEmpty
    private List<Entry> entries;

    @Data
    public static class Entry {
        @NotNull
        private UUID accountId;

        @NotNull
        private String currency;

        //Must be non-zer
        @NotNull
        private Long amountMinor;
    }
}
