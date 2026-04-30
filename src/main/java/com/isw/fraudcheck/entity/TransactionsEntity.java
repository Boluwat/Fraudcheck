package com.isw.fraudcheck.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsEntity {

    private String merchantId;
    private String cardNumber;           // will hold masked value
    private String cardNumberHashed;
    private Double transactionAmount;
    private String ipAddress;
    private int riskScore;
    private boolean isFraudulent;
    private LocalDateTime createdAt;
    private String status;
}