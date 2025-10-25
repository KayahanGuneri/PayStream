package com.paystream.paymentservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.paymentservice.api.dto.AuthorizeRequest;
import com.paystream.paymentservice.app.impl.PaymentApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PURPOSE:
 * Web layer test for PaymentController.
 * We validate that JSON body is bound to DTO and Idempotency-Key header is forwarded to service.
 *
 * NOTES for beginners:
 * - @WebMvcTest starts only the web layer (Controller + validation + advice).
 * - We mock the application service to isolate the controller.
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean PaymentApplicationService service; // or PaymentUseCase, depending on your controller

    @Test
    @DisplayName("POST /v1/payments/authorize -> 200 and service called with (req, idemKey)")
    void authorize_ok() throws Exception {
        var dto = new AuthorizeRequest("merchant-1", new BigDecimal("42.00"), "TRY", "card-t-1");

        mvc.perform(post("/v1/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "idem-xyz")
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // controller should pass both req and header to the service
        verify(service).authorize(any(AuthorizeRequest.class), eq("idem-xyz"));
    }

    @Test
    @DisplayName("POST /v1/payments/authorize -> 400 when required fields missing")
    void authorize_validation_error() throws Exception {
        // amount missing -> should trigger validation error -> 400
        var invalidJson = """
           {"merchantId":"m-1","currency":"TRY","cardToken":"t"}
        """;

        mvc.perform(post("/v1/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
