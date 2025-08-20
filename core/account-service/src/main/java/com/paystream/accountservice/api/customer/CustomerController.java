package com.paystream.accountservice.api.customer;

import com.paystream.accountservice.app.customer.CustomerAppService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Customer HTTP endpoints (live inside account-service for now).
 * This keeps SRP at class level and is easy to extract later.
 */
@RestController
@RequestMapping("/v1/customers")
public class CustomerController {

    private final CustomerAppService service;

    public CustomerController(CustomerAppService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CreateCustomerRequest req) {
        var resp = service.register(req.name(), req.email(), req.password());
        return ResponseEntity.created(URI.create("/v1/customers/" + resp.id())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
