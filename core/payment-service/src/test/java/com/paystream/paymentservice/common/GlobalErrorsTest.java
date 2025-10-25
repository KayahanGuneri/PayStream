package com.paystream.paymentservice.common;

import com.paystream.paymentservice.api.PaymentController;
import com.paystream.paymentservice.app.impl.PaymentApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PURPOSE:
 * Ensure GlobalErrors maps validation problems to 400 Bad Request.
 *
 * NOTES:
 * - We trigger MethodArgumentNotValidException by omitting required fields in JSON.
 */
@WebMvcTest(PaymentController.class)
class GlobalErrorsTest {

    @Autowired MockMvc mvc;
    @MockBean PaymentApplicationService service; // controller dependency

    @Test
    @DisplayName("Invalid body -> 400 Bad Request handled by GlobalErrors")
    void invalid_body_returns_400() throws Exception {
        var invalidJson = """
           {"merchantId":"m-1","currency":"TRY"}  // amount & cardToken missing
        """;

        mvc.perform(post("/v1/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
