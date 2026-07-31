package com.sentinel.backend.fraud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudResult {

    private int riskScore;
    private String decision;
    private String reason;
}
