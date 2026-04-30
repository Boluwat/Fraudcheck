package com.isw.fraudcheck.controller;


import com.isw.fraudcheck.DTOs.TransactionsRequestDTO;
import com.isw.fraudcheck.DTOs.TransactionsResponseDTO;
import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.service.AdminService;
import com.isw.fraudcheck.service.FraudEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions/")
public class TransactionController {

    private final FraudEngineService fraudEngineService;
    private final AdminService adminService;

    public TransactionController(FraudEngineService fraudEngineService, AdminService adminService) {
        this.fraudEngineService = fraudEngineService;
        this.adminService = adminService;
    }

    @PostMapping("/ingestAPI")
    public TransactionsResponseDTO createTransaction(@RequestBody TransactionsRequestDTO transactionsRequestDTO) {
        return fraudEngineService.processTransaction(transactionsRequestDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-blaclistedMerchant")
    public List<BlackListedMerchant> allBlackListedMerchant() {
        return adminService.getBlackListedMerchants();
    }


    @GetMapping("/allFlaggedTransactions")
    public List<TransactionsEntity> allFlaggedTransactions() {
        return adminService.getTransactions();
    }



}
