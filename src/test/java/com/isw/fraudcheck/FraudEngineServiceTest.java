package com.isw.fraudcheck;

import com.isw.fraudcheck.DTOs.TransactionsRequestDTO;
import com.isw.fraudcheck.DTOs.TransactionsResponseDTO;
import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.repository.BlackListedMerchantRepository;
import com.isw.fraudcheck.repository.TransactionJdbcQueryRepository;
import com.isw.fraudcheck.service.FraudEngineService;
import com.isw.fraudcheck.utils.IpRequestFlagger;
import com.isw.fraudcheck.utils.TransactionJdbcLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FraudEngineServiceTest {
    @Mock
    private TransactionJdbcQueryRepository transactionJdbcQueryRepository;

    @Mock
    private BlackListedMerchantRepository blackListedMerchantRepository;

    @Mock
    private TransactionJdbcLog transactionJdbcLog;

    @Mock
    private IpRequestFlagger ipRequestFlagger;

    @InjectMocks
    private FraudEngineService fraudEngineService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private TransactionsRequestDTO buildRequest(Double amount) {
        TransactionsRequestDTO dto = new TransactionsRequestDTO();
        dto.setMerchantId("MERCHANT_1");
        dto.setCardNumber("4111111111111111");
        dto.setAmount(amount);
        dto.setIpAddress("1.2.3.4");
        return dto;
    }

    private TransactionsEntity buildTransactionEntity(double amount, LocalDateTime createdAt) {
        TransactionsEntity e = new TransactionsEntity();
        e.setMerchantId("MERCHANT_1");
        e.setCardNumber("hashed");
        e.setTransactionAmount(amount);
        e.setIpAddress("1.2.3.4");
        e.setRiskScore(0);
        e.setCreatedAt(createdAt);
        e.setStatus("APPROVED");
        return e;
    }

    @Test
    void processTransaction_shouldReturnFraudWhenMerchantAlreadyBlacklisted() {
        TransactionsRequestDTO request = buildRequest(100_000.0);

        when(blackListedMerchantRepository.existsById("MERCHANT_1")).thenReturn(true);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("FRAUD", response.getStatus());
        assertEquals(100, response.getRiskScore());
        assertTrue(response.getMessage().contains("blacklisted"));

        // Log in the transactions
        verify(transactionJdbcLog, times(1)).log(any(TransactionsEntity.class));

        // this is to not fetch from db
        verify(transactionJdbcQueryRepository, never()).findRecentByMerchant(anyString(), any());
    }

    @Test
    void processTransaction_shouldApproveLowRiskTransaction() {
        TransactionsRequestDTO request = buildRequest(10_000.0);

        when(blackListedMerchantRepository.existsById("MERCHANT_1")).thenReturn(false);
        when(transactionJdbcQueryRepository.findRecentByMerchant(eq("MERCHANT_1"), any()))
                .thenReturn(Collections.emptyList());
        when(transactionJdbcQueryRepository.findRecentByCard(anyString(), any()))
                .thenReturn(Collections.emptyList());
        when(transactionJdbcQueryRepository.findRecentByIp(eq("1.2.3.4"), any()))
                .thenReturn(Collections.emptyList());
        when(ipRequestFlagger.isFlagged("1.2.3.4")).thenReturn(false);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("APPROVED", response.getStatus());
        assertEquals(0, response.getRiskScore()); // no risk signals
        verify(transactionJdbcLog, times(1)).log(any(TransactionsEntity.class));
        verify(blackListedMerchantRepository, never()).save(any(BlackListedMerchant.class));
    }

    @Test
    void processTransaction_shouldFlagHighRiskTransaction() {
        // Using a high amount and simulate card velocity to exceed fraud threshold
        TransactionsRequestDTO request = buildRequest(1_000_000.0);

        when(blackListedMerchantRepository.existsById("MERCHANT_1")).thenReturn(false);

        // Simulate a lot of recent card transactions to push score up
        List<TransactionsEntity> cardRecent1min = List.of(
                buildTransactionEntity(100_000, LocalDateTime.now().minusSeconds(30)),
                buildTransactionEntity(120_000, LocalDateTime.now().minusSeconds(20)),
                buildTransactionEntity(130_000, LocalDateTime.now().minusSeconds(10)),
                buildTransactionEntity(110_000, LocalDateTime.now().minusSeconds(5)),
                buildTransactionEntity(150_000, LocalDateTime.now().minusSeconds(1))
        );

        when(transactionJdbcQueryRepository.findRecentByMerchant(eq("MERCHANT_1"), any()))
                .thenReturn(Collections.emptyList()); // both 5min and 10min calls -> same stub is fine
        when(transactionJdbcQueryRepository.findRecentByCard(anyString(), any()))
                .thenReturn(cardRecent1min) // 1min
                .thenReturn(cardRecent1min); // 24h
        when(transactionJdbcQueryRepository.findRecentByIp(eq("1.2.3.4"), any()))
                .thenReturn(Collections.emptyList());
        when(ipRequestFlagger.isFlagged("1.2.3.4")).thenReturn(false);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("FRAUD", response.getStatus());
        assertTrue(response.getRiskScore() >= 70);
        verify(transactionJdbcLog, times(1)).log(any(TransactionsEntity.class));
    }

    @Test
    void processTransaction_shouldSetReviewStatusForMediumRisk() {
        TransactionsRequestDTO request = buildRequest(200_000.0);

        when(blackListedMerchantRepository.existsById("MERCHANT_1")).thenReturn(false);

        // Simulate some 24h transactions to trigger average spike and 24h total spend
        List<TransactionsEntity> cardRecent24h = List.of(
                buildTransactionEntity(50_000, LocalDateTime.now().minusHours(1)),
                buildTransactionEntity(60_000, LocalDateTime.now().minusHours(2)),
                buildTransactionEntity(70_000, LocalDateTime.now().minusHours(3))
        );

        when(transactionJdbcQueryRepository.findRecentByMerchant(eq("MERCHANT_1"), any()))
                .thenReturn(Collections.emptyList());
        when(transactionJdbcQueryRepository.findRecentByCard(anyString(), any()))
                .thenReturn(Collections.emptyList())     // 1min
                .thenReturn(cardRecent24h);              // 24h
        when(transactionJdbcQueryRepository.findRecentByIp(eq("1.2.3.4"), any()))
                .thenReturn(Collections.emptyList());
        when(ipRequestFlagger.isFlagged("1.2.3.4")).thenReturn(false);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertTrue(response.getRiskScore() >= 40);
        assertTrue(response.getRiskScore() < 70);
        assertEquals("REVIEW", response.getStatus());
        verify(transactionJdbcLog, times(1)).log(any(TransactionsEntity.class));
    }

    @Test
    void processTransaction_shouldAutoBlacklistMerchantOnVeryHighScore() {
        TransactionsRequestDTO request = buildRequest(1_000_000.0);

        when(blackListedMerchantRepository.existsById("MERCHANT_1")).thenReturn(false);

        // Configure 5min merchant velocity very high
        List<TransactionsEntity> merchantRecent5min = List.of(
                buildTransactionEntity(10_000, LocalDateTime.now().minusMinutes(1)),
                buildTransactionEntity(10_000, LocalDateTime.now().minusMinutes(2)),
                buildTransactionEntity(10_000, LocalDateTime.now().minusMinutes(3)),
                buildTransactionEntity(10_000, LocalDateTime.now().minusMinutes(4)),
                buildTransactionEntity(10_000, LocalDateTime.now().minusMinutes(4)),
                buildTransactionEntity(10_000, LocalDateTime.now().minusMinutes(4))
        );
        List<TransactionsEntity> emptyList = Collections.emptyList();

        when(transactionJdbcQueryRepository.findRecentByMerchant(eq("MERCHANT_1"), any()))
                .thenReturn(merchantRecent5min)   // 5min window
                .thenReturn(emptyList);           // 10min window
        when(transactionJdbcQueryRepository.findRecentByCard(anyString(), any()))
                .thenReturn(emptyList)            // 1min
                .thenReturn(emptyList);           // 24h
        when(transactionJdbcQueryRepository.findRecentByIp(eq("1.2.3.4"), any()))
                .thenReturn(emptyList);
        when(ipRequestFlagger.isFlagged("1.2.3.4")).thenReturn(true); // push score high

        when(blackListedMerchantRepository.existsById("MERCHANT_1")).thenReturn(false);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("FRAUD", response.getStatus());
        assertTrue(response.getRiskScore() >= 70);
        verify(blackListedMerchantRepository, times(1)).save(any(BlackListedMerchant.class));
        verify(transactionJdbcLog, times(1)).log(any(TransactionsEntity.class));
    }

    @Test
    void hashCardNumber_shouldReturnSameHashForSameCardAndNotEmpty() {
        String card = "4111-1111-1111-1111";
        String hash1 = FraudEngineService.hashCardNumber(card);
        String hash2 = FraudEngineService.hashCardNumber(card);

        assertNotNull(hash1);
        assertFalse(hash1.isBlank());
        assertEquals(hash1, hash2);
    }

    @Test
    void hashCardNumber_shouldReturnEmptyForNullOrBlank() {
        assertEquals("", FraudEngineService.hashCardNumber(null));
        assertEquals("", FraudEngineService.hashCardNumber("   "));
    }
}
