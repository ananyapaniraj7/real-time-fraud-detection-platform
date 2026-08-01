package com.sentinel.backend.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinel.backend.dto.CreateTransactionRequest;
import com.sentinel.backend.dto.CreateTransactionResponse;
import com.sentinel.backend.fraud.FraudResult;
import com.sentinel.backend.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<CreateTransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        FraudResult result = transactionService.createTransaction(request);
        CreateTransactionResponse response = CreateTransactionResponse.builder()
                .message("Transaction created successfully")
                .riskScore(result.getRiskScore())
                .decision(result.getDecision())
                .reason(result.getReason())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }
}
