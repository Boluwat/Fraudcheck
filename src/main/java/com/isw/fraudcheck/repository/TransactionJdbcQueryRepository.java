package com.isw.fraudcheck.repository;

import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.utils.TransactionRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

//public interface TransactionJdbcQueryRepository extends JpaRepository<TransactionsEntity, String> {
//    @Query("SELECT t FROM TransactionsEntity t WHERE t.merchantId = :merchantId AND t.transactionDate > :since")
//    List<TransactionsEntity> findRecentByMerchant(String merchantId, LocalDateTime since);
//
//    @Query("SELECT t FROM TransactionsEntity t WHERE t.cardNumber = :cardNumber AND t.transactionDate > :since")
//    List<TransactionsEntity> findRecentByCard(String cardNumber, LocalDateTime since);
//}

@Repository
public class TransactionJdbcQueryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionRowMapper rowMapper = new TransactionRowMapper();

    public TransactionJdbcQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TransactionsEntity> findRecentByMerchant(String merchantId, LocalDateTime since) {
        String sql = """
        SELECT merchant_id, card_hash, amount, ip_address, 
               risk_score, is_fraudulent, created_at, status
        FROM transactions 
        WHERE merchant_id = ? 
          AND created_at >= ?
        ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, rowMapper, merchantId, since);
    }

    public List<TransactionsEntity> findRecentByCard(String maskedCardNumber, LocalDateTime since) {
        String sql = """
        SELECT merchant_id, card_hash, amount, ip_address, 
               risk_score, is_fraudulent, created_at, status
        FROM transactions 
        WHERE card_hash = ? 
          AND created_at >= ?
        ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, rowMapper, maskedCardNumber, since);
    }

    public List<TransactionsEntity>findRecentByIp(String ipAddress, LocalDateTime since) {
        String sql = """
        SELECT merchant_id, card_hash, amount, ip_address, 
               risk_score, is_fraudulent, created_at, status
        FROM transactions 
        WHERE ip_address = ? 
          AND created_at >= ?
        ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, rowMapper, ipAddress, since);
    }

    public List<TransactionsEntity> findByStatus() {
        String sql = """
        SELECT *
        FROM transactions 
        WHERE status IN ('FRAUD', 'REVIEW')
        ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, rowMapper);
    }

}