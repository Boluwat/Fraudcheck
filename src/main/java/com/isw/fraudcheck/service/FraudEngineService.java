package com.isw.fraudcheck.service;


import com.isw.fraudcheck.DTOs.*;
import com.isw.fraudcheck.ErrorHandling.BadRequestException;
import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.repository.BlackListedMerchantRepository;
import com.isw.fraudcheck.repository.TransactionJdbcQueryRepository;
import com.isw.fraudcheck.utils.IpRequestFlagger;
import com.isw.fraudcheck.utils.TransactionJdbcLog;
import jakarta.transaction.Transactional;
import org.aspectj.lang.annotation.Around;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudEngineService {

    private static final int SCORE_IP_DISTINCT_CARDS = 10;
    private static final int SCORE_MERCHANT_10MIN = 10;
    private static final int SCORE_CARD_1MIN = 30;
    private static final int SCORE_CARD_24H = 5;
    private static final int SCORE_AMOUNT_SPIKE = 5;
    private static final int SCORE_IP_FLAGGED = 35;
    private static final int SCORE_MERCHANT_MANY_TINY = 3;

    private static final int CARD_1MIN_THRESHOLD = 5;
    private static final int MERCHANT_10MIN_THRESHOLD = 5;
    private static final int MERCHANT_5MIN_VERY_HIGH_THRESHOLD = 6;
    private static final int CARD_24H_THRESHOLD = 10;
    private static final int IP_DISTINCT_CARD_THRESHOLD = 3;

    private static final long MERCHANT_5MIN_WINDOW_MIN = 5;
    private static final long MERCHANT_10MIN_WINDOW_MIN = 10;
    private static final long CARD_1MIN_WINDOW_MIN = 1;
    private static final long CARD_24H_WINDOW_HOURS = 24;
    private static final long IP_10MIN_WINDOW_MIN = 10;

    private static final int AMOUNT_HIGH_SINGLE = 500_000;
    private static final int AMOUNT_SIGNIFICANT = 150_000;
    private static final int AMOUNT_TINY = 50_000;
    private static final double AMOUNT_SPIKE_MULTIPLIER = 5.0;
    private static final double AMOUNT_24H_TOTAL_THRESHOLD = 500_000;

    private static final int SCORE_BLACKLIST_THRESHOLD = 85;
    private static final int SCORE_FRAUD_THRESHOLD = 70;
    private static final int SCORE_REVIEW_THRESHOLD = 50;
    private static final int SCORE_24H_TOTAL_SPEND = 2;


    private final TransactionJdbcQueryRepository queryRepository;
    private final BlackListedMerchantRepository blackListedMerchantRepository;
    private final TransactionJdbcLog transactionJdbcLog;
    private final IpRequestFlagger ipRequestFlagger;

    public FraudEngineService(TransactionJdbcQueryRepository transactionJdbcQueryRepository,
                              BlackListedMerchantRepository blackListedMerchantRepository, TransactionJdbcLog transactionJdbcLog, IpRequestFlagger ipRequestFlagger) {
        this.queryRepository = transactionJdbcQueryRepository;
        this.blackListedMerchantRepository = blackListedMerchantRepository;
        this.transactionJdbcLog = transactionJdbcLog;
        this.ipRequestFlagger = ipRequestFlagger;
    }


    @Transactional
    public TransactionsResponseDTO processTransaction(TransactionsRequestDTO request) {

        String merchantId = request.getMerchantId();
        boolean isMerchantBlacklisted = blackListedMerchantRepository.existsById(merchantId);
        String cardHash = hashCardNumber(request.getCardNumber());
        LocalDateTime now = LocalDateTime.now();
        String ip = request.getIpAddress();

        int score = 0;
        String status;
        String message;

        if (isMerchantBlacklisted) {
            status = "FRAUD";
            score = 100;
            message = "Merchant has been blacklisted";
        } else {
            MerchantsStats merchantStats = queryRepository.getMerchantStats(
                    merchantId,
                    now.minusMinutes(MERCHANT_5MIN_WINDOW_MIN),
                    now.minusMinutes(MERCHANT_10MIN_WINDOW_MIN),
                    AMOUNT_TINY
            );

            CardStats cardStats = queryRepository.getCardStats(
                    cardHash,
                    now.minusMinutes(CARD_1MIN_WINDOW_MIN),
                    now.minusHours(CARD_24H_WINDOW_HOURS)
            );

            IpStats ipStats = queryRepository.getIpStats(
                    ip,
                    now.minusMinutes(IP_10MIN_WINDOW_MIN)
            );

            score = computeRiskScore(request, merchantStats, cardStats, ipStats);

            if (shouldBlacklistMerchant(score, merchantStats.txLast5MinCount())) {
                blacklistMerchantIfNeeded(merchantId, score);
                status = "FRAUD";
                message = "Transaction declined & merchant blacklisted";
            } else if (score >= SCORE_FRAUD_THRESHOLD) {
                status = "DECLINED_HIGH_RISK";
                message = "Transaction declined – high fraud risk";
            } else if (score >= SCORE_REVIEW_THRESHOLD) {
                status = "REVIEW";
                message = "Transaction flagged for manual review";
            } else {
                status = "APPROVED";
                message = "Transaction processed";
            }
        }

        TransactionsEntity txn = buildTransactionEntity(request, cardHash, score, now, status);
        transactionJdbcLog.log(txn);

        return new TransactionsResponseDTO(status, score, message);

    }

    private String maskedCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) return "INVALID";

        String cleaned = cardNumber.replaceAll("\\D", "");

        int cardLength = cleaned.length();

        if (cardLength <= 6) {
            return cleaned;
        }

        String firstSix = cleaned.substring(0, 6);
        String lastFour = cleaned.substring(cardLength - 4);

        int middleLen = cardLength - 10;

        String maskedMiddle = "*".repeat(Math.max(0, middleLen));

        return firstSix + maskedMiddle + lastFour;

    }

    public static String hashCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) return "";

        String cleaned = cardNumber.replaceAll("\\D", "").trim();

        if (cleaned.length() < 13 || cleaned.length() > 19) {
            throw new BadRequestException("Invalid card number length. Must be between 13 and 19 digits.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(cleaned.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private int computeRiskScore(TransactionsRequestDTO request,
                                MerchantsStats merchantsStats,
                                 CardStats cardStats,
                                 IpStats ipStats) {

        int score = 0;
        Double amount = request.getAmount();

        // Distinct cards from same IP (card number already hashed in the DB)
        long distinctCardsFromIp = ipStats.distinctCardsLast10Min();
        if (distinctCardsFromIp >= IP_DISTINCT_CARD_THRESHOLD) {
            score += SCORE_IP_DISTINCT_CARDS;
        }

        // Merchant velocity (10 minutes)
        if (merchantsStats.txLast10MinCount() >= MERCHANT_10MIN_THRESHOLD) {
            score += SCORE_MERCHANT_10MIN;
        }

        // Card velocity (1 minute)
        if (cardStats.txLast1MinCount() >= CARD_1MIN_THRESHOLD) {
            score += SCORE_CARD_1MIN;
        }

        // Card 24h volume
        if (cardStats.txLast24hCount() >= CARD_24H_THRESHOLD) {
            score += SCORE_CARD_24H;
        }

//        total spend within 24
        double totalSpend24h = cardStats.totalAmount24h();
        if (totalSpend24h >= AMOUNT_24H_TOTAL_THRESHOLD) {
            score += SCORE_24H_TOTAL_SPEND;
        }


        // Amount spike vs. 24h average
        if (amount != null && amount > AMOUNT_SIGNIFICANT && cardStats.txLast24hCount() > 0) {
            double avg24hAmount = cardStats.avgAmount24h();
            if (avg24hAmount > 0 && amount > avg24hAmount * AMOUNT_SPIKE_MULTIPLIER) {
                score += SCORE_AMOUNT_SPIKE;
            }
        }

        // IP flagged by external mechanism
        if (ipRequestFlagger.isFlagged(request.getIpAddress())) {
            score += SCORE_IP_FLAGGED;
        }

        // Many tiny transactions at same merchant
        if (merchantsStats.tinyTxLast5MinCount() >= MERCHANT_5MIN_VERY_HIGH_THRESHOLD
                && amount != null && amount <= AMOUNT_TINY) {
            score += SCORE_MERCHANT_MANY_TINY;
        }

        return score;
    }

    private TransactionsEntity buildTransactionEntity(TransactionsRequestDTO request,
                                                      String cardHash,
                                                      int score,
                                                      LocalDateTime now,
                                                      String status) {
        TransactionsEntity txn = new TransactionsEntity();
        txn.setMerchantId(request.getMerchantId());
        txn.setCardNumber(cardHash);
        txn.setTransactionAmount(request.getAmount());
        txn.setIpAddress(request.getIpAddress());
        txn.setRiskScore(score);
        txn.setFraudulent(score >= SCORE_FRAUD_THRESHOLD);
        txn.setCreatedAt(now);
        txn.setStatus(status);
        return txn;
    }

    private boolean shouldBlacklistMerchant(int score, long merchantRecent5minTx) {
        return score >= SCORE_BLACKLIST_THRESHOLD
                || (score >= SCORE_FRAUD_THRESHOLD && merchantRecent5minTx >= MERCHANT_5MIN_VERY_HIGH_THRESHOLD);
    }

    private void blacklistMerchantIfNeeded(String merchantId, int score) {

        // do nothing if already blacklisted
        if (blackListedMerchantRepository.existsById(merchantId)) {
            return;
        }

        BlackListedMerchant bm = new BlackListedMerchant();
        bm.setMerchantId(merchantId);
        bm.setReason("Severe fraud signals score: " + (score));
        bm.setBlackListedAt(LocalDateTime.now());
        blackListedMerchantRepository.save(bm);

//        log.warn("Merchant {} has been blacklisted — score {}", merchantId, score);
    }
}
