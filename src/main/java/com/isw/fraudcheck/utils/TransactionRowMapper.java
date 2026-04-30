package com.isw.fraudcheck.utils;

import com.isw.fraudcheck.entity.TransactionsEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRowMapper implements RowMapper<TransactionsEntity> {

    @Override
    public TransactionsEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        TransactionsEntity entity = new TransactionsEntity();

        entity.setMerchantId(rs.getString("merchant_id"));
        entity.setCardNumber(rs.getString("card_hash"));
        entity.setTransactionAmount(rs.getDouble("amount"));
        entity.setIpAddress(rs.getString("ip_address"));
        entity.setRiskScore(rs.getInt("risk_score"));
        entity.setFraudulent(rs.getBoolean("is_fraudulent"));
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        entity.setStatus(rs.getString("status"));

        return entity;
    }
}