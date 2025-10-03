// Thin REST controller: validates inputs, delegates to app service, maps Domain -> DTO.
package com.paystream.transferservice.api;

import com.paystream.transferservice.api.mapper.TransferApiMapper;
import com.paystream.transferservice.app.TransferAppService;
import com.paystream.transferservice.domain.DomainValidationException;
import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/transfers")
@Validated                      // enable Bean Validation on method params
@RequiredArgsConstructor        // constructor injection
public class TransferController {

    private final TransferAppService app;   // use-case coordinator (business flow)
    private final TransferApiMapper mapper; // maps Domain <-> DTO

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TransferResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest req) {

        if (!StringUtils.hasText(idempotencyKey)) {
            // Let GlobalExceptionHandler turn this into a 400 Problem+JSON
            throw new DomainValidationException("Missing required header: Idempotency-Key");
        }


        Transfer t = app.createTransfer(idempotencyKey.trim(), req);


        HttpStatus code = switch (t.status()) {
            case COMPLETED -> HttpStatus.CREATED;
            case PENDING, IN_PROGRESS -> HttpStatus.ACCEPTED;
            case FAILED, REVERSED -> HttpStatus.UNPROCESSABLE_ENTITY;
        };


        return ResponseEntity.status(code).body(TransferApiMapper.toResponse(t));

@Validated                      // enable Bean Validation on method parameters
@RequiredArgsConstructor        // inject dependencies via constructor
public class TransferController {

    private final TransferAppService app;
    private final TransferApiMapper mapper;

    @PostMapping
    public ResponseEntity<TransferResponse> create(
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest req
    ) {
        // Guard: ensure header is not blank (extra safety on top of 'required=true')
        // NOTE: Throw a domain-level exception that your GlobalExceptionHandler maps to 400.
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new DomainValidationException("Missing required header: Idempotency-Key");
        }

        // Delegate to application service (use-case coordinator)
        Transfer t = app.createTransfer(idempotencyKey, req);

        // Decide HTTP status code from the domain status (keep mapping centralized here)
        HttpStatus code = switch (t.status()) {              // use accessor, not field
            case COMPLETED -> HttpStatus.CREATED;            // 201 when synchronous success
            case PENDING, IN_PROGRESS -> HttpStatus.ACCEPTED;// 202 when async/in-flight
            case FAILED, REVERSED -> HttpStatus.UNPROCESSABLE_ENTITY; // 422 when business fail
        };

        // Map Domain -> DTO and return
        return ResponseEntity.status(code).body(mapper.toResponse(t));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getById(@PathVariable UUID id) {


        // NOTE: app.getById throws NotFoundException; handler maps it to 404
        Transfer t = app.getById(id);
        return ResponseEntity.ok(mapper.toResponse(t));
    }
}
