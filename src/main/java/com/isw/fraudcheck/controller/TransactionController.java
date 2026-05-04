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

    private final FraudEngineService fraudEngineService;
    private final AdminService adminService;
    private final TransactionService transactionService;

    public TransactionController(FraudEngineService fraudEngineService, AdminService adminService, TransactionService transactionService) {
        this.fraudEngineService = fraudEngineService;
        this.adminService = adminService;
        this.transactionService = transactionService;
    }

    @PostMapping("/ingestAPI")
    public TransactionsResponseDTO createTransaction(@RequestBody TransactionsRequestDTO transactionsRequestDTO) {
        return fraudEngineService.processTransaction(transactionsRequestDTO);
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
