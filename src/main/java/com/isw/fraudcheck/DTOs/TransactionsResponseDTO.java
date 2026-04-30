package com.isw.fraudcheck.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionsResponseDTO {
    private String status;
    private int riskScore;
    private String message;

    public TransactionsResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
