package com.sentinel.backend.service;

import org.springframework.stereotype.Service;

import com.sentinel.backend.dto.CreateTransactionRequest;
import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudDetectionService;
import com.sentinel.backend.fraud.FraudResult;
import com.sentinel.backend.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    public TransactionService(TransactionRepository transactionRepository,FraudDetectionService fraudDetectionService) {
        this.transactionRepository = transactionRepository;
        this.fraudDetectionService=fraudDetectionService;
    }

    
    public FraudResult createTransaction(CreateTransactionRequest request){
        Transaction transaction = Transaction.builder()
                .customerId(request.getCustomerId())
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        FraudResult result = fraudDetectionService.evaluate(transaction);

        transactionRepository.save(transaction);

        return result;
    }
}
