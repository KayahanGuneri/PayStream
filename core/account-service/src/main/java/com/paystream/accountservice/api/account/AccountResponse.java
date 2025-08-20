package com.paystream.accountservice.api.account;
import java.util.UUID;
public record AccountResponse(UUID id, String currency, String status) {}
