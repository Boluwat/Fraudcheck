package com.isw.fraudcheck.controller;


import com.isw.fraudcheck.DTOs.TransactionsRequestDTO;
import com.isw.fraudcheck.DTOs.TransactionsResponseDTO;
import com.isw.fraudcheck.service.FraudEngineService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class FraudEngineController {
    private final FraudEngineService fraudEngineService;

    public FraudEngineController(FraudEngineService fraudEngineService) {
        this.fraudEngineService = fraudEngineService;
    }

    @PostMapping("/ingest")
    public TransactionsResponseDTO createTransaction(@RequestBody TransactionsRequestDTO transactionsRequestDTO) {
        return fraudEngineService.processTransaction(transactionsRequestDTO);
    }
}
