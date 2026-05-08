package com.isw.fraudcheck.controller;


import com.isw.fraudcheck.DTOs.TransactionsRequestDTO;
import com.isw.fraudcheck.DTOs.TransactionsResponseDTO;
import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.service.AdminService;
import com.isw.fraudcheck.service.FraudEngineService;
import com.isw.fraudcheck.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/transactions/")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/all-blaclistedMerchant")
    public List<BlackListedMerchant> allBlackListedMerchant() {
        return transactionService.getBlackListedMerchants();
    }


    @GetMapping("/allFlaggedTransactions")
    public List<TransactionsEntity> allFlaggedTransactions() {
        return transactionService.getTransactions();
    }



}
