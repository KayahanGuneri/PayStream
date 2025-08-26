// Minimal structured error body for REST errors.
package com.paystream.transferservice.api;

public record ErrorResponse(String code, String message) {}
