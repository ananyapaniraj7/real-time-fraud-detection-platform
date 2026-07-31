package com.sentinel.backend.fraud.rules;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudResult;

public interface FraudRule {

    FraudResult evaluate(Transaction transaction);
}
