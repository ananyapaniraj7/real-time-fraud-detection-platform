package com.sentinel.backend.fraud.rules;

import org.springframework.stereotype.Component;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudDecision;
import com.sentinel.backend.fraud.FraudResult;

@Component
public class ForeignCurrencyRule implements FraudRule {

    private static final String DOMESTIC_CURRENCY = "INR";

    @Override
    public FraudResult evaluate(Transaction transaction) {
        if (!DOMESTIC_CURRENCY.equalsIgnoreCase(transaction.getCurrency())) {
            return FraudResult.builder()
                    .riskScore(25)
                    .decision(FraudDecision.REVIEW)
                    .reason("Foreign currency transaction")
                    .build();
        }

        return FraudResult.builder()
                .riskScore(0)
                .decision(FraudDecision.ALLOW)
                .reason("Domestic currency transaction")
                .build();
    }
}
