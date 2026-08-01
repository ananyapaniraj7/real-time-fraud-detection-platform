package com.sentinel.backend.fraud.rules;

import java.util.Set;
import org.springframework.stereotype.Component;
import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudDecision;
import com.sentinel.backend.fraud.FraudResult;

@Component

public class MerchantBlackListRule implements FraudRule{
    private static final Set<String> BLACKLISTED_MERCHANT_IDS= Set.of(
        "BLACKLIST001",
        "BLACKLIST002",
        "FRAUDMERCHANT" 
    );

    @Override
    public FraudResult evaluate(Transaction transaction){
        if(BLACKLISTED_MERCHANT_IDS.contains(transaction.getMerchantId())){
            return FraudResult.builder()
                    .riskScore(100)
                    .decision(FraudDecision.BLOCK)
                    .reason("Merchant is blacklisted")
                    .build();
        }

        return FraudResult.builder()
                .riskScore(0)
                .decision(FraudDecision.ALLOW)
                .reason("Merchant is not blacklisted")
                .build();
    }
}
