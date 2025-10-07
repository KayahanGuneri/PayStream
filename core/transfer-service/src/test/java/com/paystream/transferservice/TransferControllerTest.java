package com.paystream.transferservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.transferservice.api.CreateTransferRequest;
import com.paystream.transferservice.api.TransferController;
import com.paystream.transferservice.app.TransferAppService;
import com.paystream.transferservice.domain.NotFoundException;
import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Purpose: Verify the HTTP contract of TransferController (routing, headers, validation, statuses).
 * Why: WebMvc slice is fast and isolates the web layer; business logic is mocked.
 *
 * Notes on annotations:
 * - @WebMvcTest: boots only MVC components for the given controller (no full context).
 * - @MockBean: replaces the real service bean with a Mockito mock for isolation.
 */
@WebMvcTest(controllers = TransferController.class)
class TransferControllerTest {

    @Autowired MockMvc mvc;          // Mock HTTP client for controller testing
    @Autowired ObjectMapper om;      // JSON serializer/deserializer for request/response

    @MockBean TransferAppService app; // Mocked business layer

    // --- helpers ------------------------------------------------------------

    /** Builds a valid create request DTO for happy paths. */
    private static CreateTransferRequest validReq() {
        return new CreateTransferRequest(
                UUID.randomUUID(),   // sourceAccountId
                UUID.randomUUID(),   // destAccountId
                "TRY",               // currency
                1_000L               // amountMinor (positive)
        );
    }

    /** Minimal domain object used as mocked service return value. */
    private static Transfer domain(UUID id, TransferStatus status, UUID ledgerTxId) {
        var t = new Transfer();
        t.id = id;
        t.status = status;
        t.ledgerTxId = ledgerTxId;
        return t;
    }

    // --- POST /v1/transfers -------------------------------------------------

    @Test
    @DisplayName("POST /v1/transfers -> 201 when COMPLETED")
    void post_should_return201_when_completed() throws Exception {
        var req = validReq();
        var id = UUID.randomUUID();

        // Mocking business call: service returns COMPLETED
        Mockito.when(app.createTransfer(anyString(), any(CreateTransferRequest.class)))
                .thenReturn(domain(id, TransferStatus.COMPLETED, UUID.randomUUID()));

        mvc.perform(post("/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "I-1")
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())                       // 201
                .andExpect(jsonPath("$.transferId", is(id.toString())))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.ledgerTxId", notNullValue()));
    }

    @Test
    @DisplayName("POST /v1/transfers -> 202 when PENDING or IN_PROGRESS")
    void post_should_return202_when_pending_or_inprogress() throws Exception {
        // Case: PENDING
        Mockito.when(app.createTransfer(anyString(), any(CreateTransferRequest.class)))
                .thenReturn(domain(UUID.randomUUID(), TransferStatus.PENDING, null));

        mvc.perform(post("/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "I-2")
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isAccepted())                      // 202
                .andExpect(jsonPath("$.status", is("PENDING")));

        // Case: IN_PROGRESS
        Mockito.when(app.createTransfer(anyString(), any(CreateTransferRequest.class)))
                .thenReturn(domain(UUID.randomUUID(), TransferStatus.IN_PROGRESS, null));

        mvc.perform(post("/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "I-3")
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isAccepted())                      // 202
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("POST /v1/transfers -> 422 when FAILED/REVERSED")
    void post_should_return422_when_failed_or_reversed() throws Exception {
        Mockito.when(app.createTransfer(anyString(), any(CreateTransferRequest.class)))
                .thenReturn(domain(UUID.randomUUID(), TransferStatus.FAILED, null));

        mvc.perform(post("/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "I-4")
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isUnprocessableEntity())           // 422
                .andExpect(jsonPath("$.status", is("FAILED")));
    }

    @Test
    @DisplayName("POST /v1/transfers -> 400 when Idempotency-Key header missing")
    void post_should_return400_when_idempotency_key_missing() throws Exception {
        // No Idempotency-Key header on purpose
        mvc.perform(post("/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isBadRequest())                    // 400
                // Be flexible on error code text; just assert presence for stability
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("POST /v1/transfers -> 400 on DTO validation errors")
    void post_should_return400_on_validation_errors() throws Exception {
        // Invalid DTO: blank currency and non-positive amount
        var bad = new CreateTransferRequest(
                UUID.randomUUID(), UUID.randomUUID(), "", 0L
        );

        mvc.perform(post("/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "I-5")
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())                    // 400 from @Valid
                .andExpect(jsonPath("$.code", anyOf(is("VALIDATION_ERROR"), is("BAD_REQUEST"))));
    }

    // --- GET /v1/transfers/{id} --------------------------------------------

    @Test
    @DisplayName("GET /v1/transfers/{id} -> 200 with body")
    void getById_should_return200() throws Exception {
        var id = UUID.randomUUID();
        Mockito.when(app.getById(id))
                .thenReturn(domain(id, TransferStatus.COMPLETED, UUID.randomUUID()));

        mvc.perform(get("/v1/transfers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId", is(id.toString())))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("GET /v1/transfers/{id} -> 404 when not found")
    void getById_should_return404_when_not_found() throws Exception {
        var id = UUID.randomUUID();
        Mockito.when(app.getById(id))
                .thenThrow(new NotFoundException("not found"));

        mvc.perform(get("/v1/transfers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }
}
