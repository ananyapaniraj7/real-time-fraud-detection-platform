package com.sentinel.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinel.backend.dto.CreateTransactionRequest;
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
    public ResponseEntity<Map<String, String>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "Transaction created successfully"));
    }
}
