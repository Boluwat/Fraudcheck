package com.isw.fraudcheck.DTOs;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionsRequestDTO {
    private String merchantId;
    private Double amount;
    private String cardNumber;
    private String ipAddress;

}
