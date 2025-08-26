
// Thin REST controller: delegates to application service, maps Domain -> DTO.
package com.paystream.transferservice.api;

import com.paystream.transferservice.api.mapper.TransferApiMapper;
import com.paystream.transferservice.app.TransferAppService;
import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/transfers")

// REST controller that exposes the /v1/transfers endpoint
package com.paystream.transferservice.api;

import com.paystream.transferservice.app.TransferAppService;
import com.paystream.transferservice.domain.TransferStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // tells Spring: this class handles HTTP requests and returns JSON
@RequestMapping("/v1/transfers") // base path for all endpoints in this controller

public class TransferController {

    private final TransferAppService app;


    public TransferController(TransferAppService app) { this.app = app; }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest req
    ) {
        // guard: ensure header exists (extra safety beyond required header)
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("Missing required header: Idempotency-Key");
        }

        Transfer t = app.createTransfer(idempotencyKey, req);

        HttpStatus code = switch (t.status) {
            case COMPLETED -> HttpStatus.CREATED;
            case PENDING, IN_PROGRESS -> HttpStatus.ACCEPTED;
            case FAILED, REVERSED -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        return ResponseEntity.status(code).body(TransferApiMapper.toResponse(t));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Transfer t = app.getById(id);
        return ResponseEntity.ok(TransferApiMapper.toResponse(t));

    // constructor injection (preferred for testability and immutability)
    public TransferController(TransferAppService app) { this.app = app; }

    @PostMapping // handles HTTP POST /v1/transfers
    public ResponseEntity<TransferResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey, // required header
            @Valid @RequestBody CreateTransferRequest req // validate JSON body with Jakarta Validation
    ) {
        // Delegate all business logic to application service (thin controller)
        var result = app.createTransfer(idempotencyKey, req);

        // Map domain status to appropriate HTTP code
        if (result.status() == TransferStatus.COMPLETED) {
            return ResponseEntity.status(201).body(result); // Created
        } else if (result.status() == TransferStatus.PENDING || result.status() == TransferStatus.IN_PROGRESS) {
            return ResponseEntity.accepted().body(result); // 202 for async flows (future friendly)
        } else {
            return ResponseEntity.unprocessableEntity().body(result); // 422 for business errors (e.g., insufficient funds)
        }
    }
}
