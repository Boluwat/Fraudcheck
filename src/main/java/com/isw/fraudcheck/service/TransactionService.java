package com.isw.fraudcheck.service;


import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.repository.BlackListedMerchantRepository;
import com.isw.fraudcheck.repository.TransactionJdbcQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    private final BlackListedMerchantRepository blackListedMerchantRepository;
    private final TransactionJdbcQueryRepository queryRepository;

    public TransactionService(BlackListedMerchantRepository blackListedMerchantRepository, TransactionJdbcQueryRepository queryRepository) {
        this.blackListedMerchantRepository = blackListedMerchantRepository;
        this.queryRepository = queryRepository;
    }

    public List<BlackListedMerchant> getBlackListedMerchants() {
        return blackListedMerchantRepository.findAll();
    }

    public List<TransactionsEntity> getTransactions() {
        return queryRepository.findByStatus();
    }
}
