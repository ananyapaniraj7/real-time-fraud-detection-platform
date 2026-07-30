package com.sentinel.backend.service;

import org.springframework.stereotype.Service;

import com.sentinel.backend.dto.CreateTransactionRequest;
import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .customerId(request.getCustomerId())
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        return transactionRepository.save(transaction);
    }
}
