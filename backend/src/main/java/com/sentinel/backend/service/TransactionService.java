package com.sentinel.backend.service;

import java.util.List;
import com.sentinel.backend.dto.TransactionHistoryResponse;
import org.springframework.stereotype.Service;

import com.sentinel.backend.dto.CreateTransactionRequest;
import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudDetectionService;
import com.sentinel.backend.fraud.FraudResult;
import com.sentinel.backend.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    public TransactionService(TransactionRepository transactionRepository,FraudDetectionService fraudDetectionService) {
        this.transactionRepository = transactionRepository;
        this.fraudDetectionService=fraudDetectionService;
    }
    
    public FraudResult createTransaction(CreateTransactionRequest request){
        log.info(
                "Transaction received: customerId={}, merchantId={}, amount={}, currency={}",
                request.getCustomerId(),
                request.getMerchantId(),
                request.getAmount(),
                request.getCurrency()
        );

        Transaction transaction = Transaction.builder()
                .customerId(request.getCustomerId())
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();
        
        FraudResult result = fraudDetectionService.evaluate(transaction);
        transaction.setRiskScore(result.getRiskScore());
        transaction.setDecision(result.getDecision());
        transaction.setReason(result.getReason());

        transactionRepository.save(transaction);

        log.info(
                "Transaction evaluated: transactionId={}, riskScore={}, decision={}",
                transaction.getId(),
                result.getRiskScore(),
                result.getDecision()
        );

        return result;
    }
    public List<TransactionHistoryResponse> getRecentTransactions() {

        return transactionRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(transaction -> new TransactionHistoryResponse(
                        transaction.getId(),
                        transaction.getCustomerId(),
                        transaction.getMerchantId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getRiskScore(),
                        transaction.getDecision(),
                        transaction.getReason(),
                        transaction.getCreatedAt()
                ))
                .toList();
    }

}
