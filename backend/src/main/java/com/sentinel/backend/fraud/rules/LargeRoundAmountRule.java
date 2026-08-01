package com.sentinel.backend.fraud.rules;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudDecision;
import com.sentinel.backend.fraud.FraudResult;

@Component
public class LargeRoundAmountRule implements FraudRule {

    private static final BigDecimal ROUND_AMOUNT_DIVISOR = new BigDecimal("10000");

    @Override
    public FraudResult evaluate(Transaction transaction) {
        if (transaction.getAmount().remainder(ROUND_AMOUNT_DIVISOR).compareTo(BigDecimal.ZERO) == 0) {
            return FraudResult.builder()
                    .riskScore(15)
                    .decision(FraudDecision.REVIEW)
                    .reason("Large round amount")
                    .build();
        }

        return FraudResult.builder()
                .riskScore(0)
                .decision(FraudDecision.ALLOW)
                .reason("Amount pattern appears normal")
                .build();
    }
}
