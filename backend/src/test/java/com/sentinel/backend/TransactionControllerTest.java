package com.sentinel.backend;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sentinel.backend.controller.TransactionController;
import com.sentinel.backend.dto.CreateTransactionRequest;
import com.sentinel.backend.fraud.FraudDecision;
import com.sentinel.backend.fraud.FraudResult;
import com.sentinel.backend.service.TransactionService;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void createTransactionShouldReturnCreatedResponseForValidTransaction() throws Exception {
        // Arrange
        FraudResult fraudResult = FraudResult.builder()
                .riskScore(0)
                .decision(FraudDecision.ALLOW)
                .reason("No fraud indicators detected")
                .build();

        when(transactionService.createTransaction(any(CreateTransactionRequest.class))).thenReturn(fraudResult);

        String requestBody = """
                {
                  "customerId": "CUSTOMER001",
                  "merchantId": "MERCHANT001",
                  "amount": 2500.00,
                  "currency": "INR"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transaction created successfully"))
                .andExpect(jsonPath("$.riskScore").value(0))
                .andExpect(jsonPath("$.decision").value("ALLOW"))
                .andExpect(jsonPath("$.reason").value("No fraud indicators detected"));
    }

    @Test
    void createTransactionShouldReturnBadRequestForInvalidTransaction() throws Exception {
        // Arrange
        String requestBody = """
                {
                  "customerId": "",
                  "merchantId": "",
                  "amount": -10.00,
                  "currency": "IN"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.customerId").exists())
                .andExpect(jsonPath("$.fieldErrors.merchantId").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.currency").exists());
    }

    @Test
    void createTransactionShouldReturnBlockDecisionForBlacklistedMerchant() throws Exception {
        // Arrange
        FraudResult fraudResult = FraudResult.builder()
                .riskScore(100)
                .decision(FraudDecision.BLOCK)
                .reason("Merchant is blacklisted")
                .build();

        when(transactionService.createTransaction(any(CreateTransactionRequest.class))).thenReturn(fraudResult);

        String requestBody = """
                {
                  "customerId": "CUSTOMER001",
                  "merchantId": "BLACKLIST001",
                  "amount": 2500.00,
                  "currency": "INR"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riskScore").value(100))
                .andExpect(jsonPath("$.decision").value("BLOCK"))
                .andExpect(jsonPath("$.reason").value(containsString("Merchant is blacklisted")));
    }
}