package com.sentinel.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudDecision;
import com.sentinel.backend.fraud.FraudDetectionService;
import com.sentinel.backend.fraud.FraudResult;
import com.sentinel.backend.fraud.rules.ForeignCurrencyRule;
import com.sentinel.backend.fraud.rules.FraudRule;
import com.sentinel.backend.fraud.rules.HighAmountRule;
import com.sentinel.backend.fraud.rules.LargeRoundAmountRule;
import com.sentinel.backend.fraud.rules.MerchantBlackListRule;

class FraudDetectionServiceTest {

    private final FraudDetectionService fraudDetectionService = new FraudDetectionService(realRules());

    @Test
    void evaluateShouldAllowNormalInrTransactionBelowHighAmountThreshold() {
        // Arrange
        Transaction transaction = transaction("MERCHANT001", "2500.00", "INR");

        // Act
        FraudResult result = fraudDetectionService.evaluate(transaction);

        // Assert
        assertThat(result.getRiskScore()).isZero();
        assertThat(result.getDecision()).isEqualTo(FraudDecision.ALLOW);
    }

    @Test
    void evaluateShouldBlockTransactionAboveHighAmountThreshold() {
        // Arrange
        Transaction transaction = transaction("MERCHANT001", "100001.00", "INR");

        // Act
        FraudResult result = fraudDetectionService.evaluate(transaction);

        // Assert
        assertThat(result.getRiskScore()).isEqualTo(80);
        assertThat(result.getDecision()).isEqualTo(FraudDecision.BLOCK);
    }

    @Test
    void evaluateShouldAllowForeignCurrencyTransactionBelowReviewThreshold() {
        // Arrange
        Transaction transaction = transaction("MERCHANT001", "2500.00", "USD");

        // Act
        FraudResult result = fraudDetectionService.evaluate(transaction);

        // Assert
        assertThat(result.getRiskScore()).isEqualTo(25);
        assertThat(result.getDecision()).isEqualTo(FraudDecision.ALLOW);
    }

    @Test
    void evaluateShouldBlockBlacklistedMerchantTransaction() {
        // Arrange
        Transaction transaction = transaction("BLACKLIST001", "2500.00", "INR");

        // Act
        FraudResult result = fraudDetectionService.evaluate(transaction);

        // Assert
        assertThat(result.getRiskScore()).isEqualTo(100);
        assertThat(result.getDecision()).isEqualTo(FraudDecision.BLOCK);
    }

    @Test
    void evaluateShouldAllowLargeRoundAmountBelowReviewThreshold() {
        // Arrange
        Transaction transaction = transaction("MERCHANT001", "20000.00", "INR");

        // Act
        FraudResult result = fraudDetectionService.evaluate(transaction);

        // Assert
        assertThat(result.getRiskScore()).isEqualTo(15);
        assertThat(result.getDecision()).isEqualTo(FraudDecision.ALLOW);
    }

    @Test
    void evaluateShouldSumMultipleFraudIndicatorsCapRiskScoreAndBlock() {
        // Arrange
        Transaction transaction = transaction("BLACKLIST001", "120000.00", "USD");

        // Act
        FraudResult result = fraudDetectionService.evaluate(transaction);

        // Assert
        assertThat(result.getRiskScore()).isEqualTo(100);
        assertThat(result.getDecision()).isEqualTo(FraudDecision.BLOCK);
        assertThat(result.getReason())
                .contains("High transaction amount")
                .contains("Foreign currency transaction")
                .contains("Large round amount")
                .contains("Merchant is blacklisted");
    }

    private static List<FraudRule> realRules() {
        return List.of(
                new HighAmountRule(),
                new ForeignCurrencyRule(),
                new LargeRoundAmountRule(),
                new MerchantBlackListRule()
        );
    }

    private static Transaction transaction(String merchantId, String amount, String currency) {
        return Transaction.builder()
                .customerId("CUSTOMER001")
                .merchantId(merchantId)
                .amount(new BigDecimal(amount))
                .currency(currency)
                .build();
    }
}