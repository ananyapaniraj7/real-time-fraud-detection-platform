package com.sentinel.backend.fraud.rules;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudResult;

@Component
public class HighAmountRule implements FraudRule {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("100000");

    @Override
    public FraudResult evaluate(Transaction transaction) {
        if (transaction.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            return FraudResult.builder()
                    .riskScore(80)
                    .decision("REVIEW")
                    .reason("High transaction amount")
                    .build();
        }

        return FraudResult.builder()
                .riskScore(0)
                .decision("ALLOW")
                .reason("No high amount risk")
                .build();
    }
}
