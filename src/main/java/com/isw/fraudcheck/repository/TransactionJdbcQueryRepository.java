package com.isw.fraudcheck.repository;

import com.isw.fraudcheck.DTOs.CardStats;
import com.isw.fraudcheck.DTOs.IpStats;
import com.isw.fraudcheck.DTOs.MerchantsStats;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.utils.TransactionRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import java.sql.Timestamp;
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

//    public List<TransactionsEntity> findRecentByMerchant(String merchantId, LocalDateTime since) {
//        String sql = """
//        SELECT merchant_id, card_hash, amount, ip_address,
//               risk_score, is_fraudulent, created_at, status
//        FROM transactions
//        WHERE merchant_id = ?
//          AND created_at >= ?
//        ORDER BY created_at DESC
//        """;
//
//        return jdbcTemplate.query(sql, rowMapper, merchantId, since);
//    }

//    public List<TransactionsEntity> findRecentByCard(String maskedCardNumber, LocalDateTime since) {
//        String sql = """
//        SELECT merchant_id, card_hash, amount, ip_address,
//               risk_score, is_fraudulent, created_at, status
//        FROM transactions
//        WHERE card_hash = ?
//          AND created_at >= ?
//        ORDER BY created_at DESC
//        """;
//
//        return jdbcTemplate.query(sql, rowMapper, maskedCardNumber, since);
//    }

//    public List<TransactionsEntity>findRecentByIp(String ipAddress, LocalDateTime since) {
//        String sql = """
//        SELECT merchant_id, card_hash, amount, ip_address,
//               risk_score, is_fraudulent, created_at, status
//        FROM transactions
//        WHERE ip_address = ?
//          AND created_at >= ?
//        ORDER BY created_at DESC
//        """;
//
//        return jdbcTemplate.query(sql, rowMapper, ipAddress, since);
//    }

    public List<TransactionsEntity> findByStatus() {
        String sql = """
        SELECT *
        FROM transactions
        WHERE status IN ('FRAUD', 'REVIEW')
        ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    public MerchantsStats getMerchantStats(String merchantId,
                                          LocalDateTime since5Min,
                                          LocalDateTime since10Min,
                                          double tinyAmountThreshold) {

        String sql = """
        SELECT
            SUM(CASE WHEN created_at >= ? THEN 1 ELSE 0 END) AS cnt_5min,
            SUM(CASE WHEN created_at >= ? THEN 1 ELSE 0 END) AS cnt_10min,
            SUM(CASE WHEN created_at >= ? AND amount <= ? THEN 1 ELSE 0 END) AS tiny_5min
        FROM transactions
        WHERE merchant_id = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new MerchantsStats(
                                rs.getLong("cnt_5min"),
                                rs.getLong("cnt_10min"),
                                rs.getLong("tiny_5min")
                        ),
                Timestamp.valueOf(since5Min),
                Timestamp.valueOf(since10Min),
                Timestamp.valueOf(since5Min),
                tinyAmountThreshold,
                merchantId
        );
    }

    public CardStats getCardStats(String cardHash,
                                  LocalDateTime since1Min,
                                  LocalDateTime since24h) {

        String sql = """
        SELECT
            SUM(CASE WHEN created_at >= ? THEN 1 ELSE 0 END) AS cnt_1min,
            SUM(CASE WHEN created_at >= ? THEN 1 ELSE 0 END) AS cnt_24h,
            SUM(CASE WHEN created_at >= ? THEN amount ELSE 0 END) AS total_24h,
            AVG(CASE WHEN created_at >= ? THEN amount END) AS avg_24h
        FROM transactions
        WHERE card_hash = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new CardStats(
                                rs.getLong("cnt_1min"),
                                rs.getLong("cnt_24h"),
                                rs.getDouble("total_24h"),
                                rs.getDouble("avg_24h")
                        ),
                Timestamp.valueOf(since1Min),
                Timestamp.valueOf(since24h),
                Timestamp.valueOf(since24h),
                Timestamp.valueOf(since24h),
                cardHash
        );
    }

    public IpStats getIpStats(String ipAddress,
                              LocalDateTime since10Min) {

        String sql = """
        SELECT
            COUNT(*) AS cnt_10min,
            COUNT(DISTINCT card_hash) AS distinct_cards_10min
        FROM transactions
        WHERE ip_address = ?
          AND created_at >= ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new IpStats(
                                rs.getLong("cnt_10min"),
                                rs.getLong("distinct_cards_10min")
                        ),
                ipAddress,
                Timestamp.valueOf(since10Min)
        );
    }

}