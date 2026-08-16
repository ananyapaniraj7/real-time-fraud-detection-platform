package com.sentinel.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentinel.backend.dto.CreateTransactionRequest;
import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudDecision;
import com.sentinel.backend.fraud.FraudDetectionService;
import com.sentinel.backend.fraud.FraudResult;
import com.sentinel.backend.repository.TransactionRepository;
import com.sentinel.backend.service.TransactionService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransactionShouldEvaluateFraudSaveTransactionAndReturnFraudResult() {
        // Arrange
        CreateTransactionRequest request = new CreateTransactionRequest(
                "CUSTOMER001",
                "MERCHANT001",
                new BigDecimal("2500.00"),
                "INR"
        );

        FraudResult expectedResult = FraudResult.builder()
                .riskScore(10)
                .decision(FraudDecision.ALLOW)
                .reason("Transaction appears normal")
                .build();

        when(fraudDetectionService.evaluate(any(Transaction.class))).thenReturn(expectedResult);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FraudResult actualResult = transactionService.createTransaction(request);

        // Assert
        assertThat(actualResult).isSameAs(expectedResult);

        verify(fraudDetectionService, times(1)).evaluate(any(Transaction.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}