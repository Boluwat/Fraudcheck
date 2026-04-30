package com.isw.fraudcheck.utils;

import com.isw.fraudcheck.entity.TransactionsEntity;
import org.springframework.jdbc.core.JdbcTemplate;   // Spring's JdbcTemplate
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionJdbcLog {

    private final JdbcTemplate jdbcTemplate;



    private static final String INSERT_SQL = """
        INSERT INTO transactions 
        (id, merchant_id, card_hash, amount, ip_address, risk_score, is_fraudulent, created_at, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

    public TransactionJdbcLog(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(TransactionsEntity txn) {
        if (txn == null) return;

        UUID id = UUID.randomUUID();

        jdbcTemplate.update(INSERT_SQL,
                id,
                txn.getMerchantId(),
                txn.getCardNumber(),                    // must be the masked value
                txn.getTransactionAmount(),
                txn.getIpAddress(),
                txn.getRiskScore(),
                txn.isFraudulent(),
                txn.getCreatedAt() != null
                        ? txn.getCreatedAt()
                        : LocalDateTime.now(),
                txn.getStatus()
        );
    }
}