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
    }
}
