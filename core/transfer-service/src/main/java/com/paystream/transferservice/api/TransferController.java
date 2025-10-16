package com.paystream.transferservice.api;

import com.paystream.transferservice.api.mapper.TransferApiMapper;
import com.paystream.transferservice.app.TransferAppService;
import com.paystream.transferservice.domain.DomainValidationException;
import com.paystream.transferservice.domain.Transfer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Transfer API.
 * Validates headers, delegates to application service, maps domain -> DTO.
 */
@RestController
@RequestMapping("/v1/transfers")
@Validated
@RequiredArgsConstructor
public class TransferController {

    private final TransferAppService app;
    private final TransferApiMapper mapper;


    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TransferResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest req) {

        if (!StringUtils.hasText(idempotencyKey)) {
            throw new DomainValidationException("Missing required header: Idempotency-Key");
        }

        Transfer t = app.createTransfer(idempotencyKey.trim(), req);

        HttpStatus code = switch (t.status()) {
            case COMPLETED -> HttpStatus.CREATED;
            case PENDING, IN_PROGRESS -> HttpStatus.ACCEPTED;
            case FAILED, REVERSED -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        return ResponseEntity.status(code).body(mapper.toResponse(t));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getById(@PathVariable UUID id) {
        Transfer t = app.getById(id);
        return ResponseEntity.ok(mapper.toResponse(t));
    }
}
