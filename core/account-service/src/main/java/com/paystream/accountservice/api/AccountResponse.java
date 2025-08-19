package com.paystream.accountservice.api;
import java.util.UUID;
public record AccountResponse(UUID id, String currency, String status) {}
