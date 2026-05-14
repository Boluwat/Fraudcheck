package com.isw.fraudcheck;

import com.isw.fraudcheck.DTOs.*;
import com.isw.fraudcheck.entity.BlackListedMerchant;
import com.isw.fraudcheck.entity.TransactionsEntity;
import com.isw.fraudcheck.repository.BlackListedMerchantRepository;
import com.isw.fraudcheck.repository.TransactionJdbcQueryRepository;
import com.isw.fraudcheck.service.FraudEngineService;
import com.isw.fraudcheck.utils.IpRequestFlagger;
import com.isw.fraudcheck.utils.TransactionJdbcLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudEngineServiceTest {

    @Mock
    private TransactionJdbcQueryRepository queryRepository;

    @Mock
    private BlackListedMerchantRepository blackListedMerchantRepository;

    @Mock
    private TransactionJdbcLog transactionJdbcLog;

    @Mock
    private IpRequestFlagger ipRequestFlagger;

    @InjectMocks
    private FraudEngineService fraudEngineService;

    @Captor
    private ArgumentCaptor<TransactionsEntity> txnCaptor;

    @Captor
    private ArgumentCaptor<BlackListedMerchant> blackListedMerchantCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    private TransactionsRequestDTO buildRequest(double amount, String merchantId, String cardNumber, String ip) {
        TransactionsRequestDTO dto = new TransactionsRequestDTO();
        dto.setAmount(amount);
        dto.setMerchantId(merchantId);
        dto.setCardNumber(cardNumber);
        dto.setIpAddress(ip);
        return dto;
    }


    private MerchantsStats merchantsStats(long txLast5, long txLast10, long tinyLast5) {
        return new MerchantsStats(txLast5, txLast10, tinyLast5);
    }


    private CardStats cardStats(long txLast1, long txLast24, double avg24, double total24) {
        return new CardStats(txLast1, txLast24, avg24, total24);
    }


    private IpStats ipStats(long distinctCards, long txLast10MinCount) {
        return new IpStats(distinctCards,  txLast10MinCount);
    }

    @Test
    void processTransaction_whenMerchantBlacklisted_shouldReturnFraudAndScore100() {
        TransactionsRequestDTO request = buildRequest(
                100_000,
                "M123",
                "5399830000000008",
                "10.0.0.1"
        );

        when(blackListedMerchantRepository.existsById("M123")).thenReturn(true);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("FRAUD", response.getStatus());
        assertEquals(100, response.getRiskScore());
        assertEquals("Merchant has been blacklisted", response.getMessage());


        verify(transactionJdbcLog).log(any(TransactionsEntity.class));

        verify(blackListedMerchantRepository, never()).save(any());
    }

    @Test
    void processTransaction_whenLowRisk_shouldReturnApproved() {
        TransactionsRequestDTO request = buildRequest(
                20_000,
                "M200",
                "5399830000000008",
                "10.0.0.2"
        );

        when(blackListedMerchantRepository.existsById("M200")).thenReturn(false);


        when(queryRepository.getMerchantStats(anyString(), any(), any(), anyDouble()))
                .thenReturn(merchantsStats(1, 1, 0));

        when(queryRepository.getCardStats(anyString(), any(), any()))
                .thenReturn(cardStats(0, 0, 0.0, 0.0));

        when(queryRepository.getIpStats(anyString(), any()))
                .thenReturn(ipStats(0, 0));

        when(ipRequestFlagger.isFlagged("10.0.0.2")).thenReturn(false);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("APPROVED", response.getStatus());
        assertTrue(response.getRiskScore() < 50);
        assertEquals("Transaction processed", response.getMessage());

        verify(transactionJdbcLog).log(txnCaptor.capture());
        TransactionsEntity savedTxn = txnCaptor.getValue();
        assertEquals("M200", savedTxn.getMerchantId());
        assertEquals(request.getAmount(), savedTxn.getTransactionAmount());
        assertEquals("10.0.0.2", savedTxn.getIpAddress());
        assertFalse(savedTxn.isFraudulent());
    }

    @Test
    void processTransaction_whenScoreTriggersBlacklist_shouldBlacklistMerchantAndReturnFraud() {
        TransactionsRequestDTO request = buildRequest(
                800_000,
                "M400",
                "5399830000000008",
                "10.0.0.4"
        );

        when(blackListedMerchantRepository.existsById("M400")).thenReturn(false);


        when(queryRepository.getMerchantStats(anyString(), any(), any(), anyDouble()))
                .thenReturn(merchantsStats(
                        10,  // txLast5MinCount
                        10,  // txLast10MinCount
                        10   // tinyTxLast5MinCount
                ));

        when(queryRepository.getCardStats(anyString(), any(), any()))
                .thenReturn(cardStats(
                        10,     // txLast1MinCount
                        30,     // txLast24hCount
                        5_000.0,  // avg24hAmount
                        1_000_000.0 // totalAmount24h
                ));

        when(queryRepository.getIpStats(anyString(), any()))
                .thenReturn(ipStats(10, 10));

        when(ipRequestFlagger.isFlagged("10.0.0.4")).thenReturn(true);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("FRAUD", response.getStatus());
        assertEquals("Transaction declined & merchant blacklisted", response.getMessage());
        assertTrue(response.getRiskScore() >= 85);


        verify(blackListedMerchantRepository).save(blackListedMerchantCaptor.capture());
        BlackListedMerchant saved = blackListedMerchantCaptor.getValue();
        assertEquals("M400", saved.getMerchantId());
        assertNotNull(saved.getBlackListedAt());
        assertTrue(saved.getReason().contains("Severe fraud signals score"));


        verify(transactionJdbcLog).log(txnCaptor.capture());
        assertTrue(txnCaptor.getValue().isFraudulent());
    }

    @Test
    void processTransaction_whenMediumRisk_shouldReturnReview() {
        TransactionsRequestDTO request = buildRequest(
                200_000,
                "M500",
                "5399830000000008",
                "10.0.0.5"
        );

        when(blackListedMerchantRepository.existsById("M500")).thenReturn(false);


        when(queryRepository.getMerchantStats(anyString(), any(), any(), anyDouble()))
                .thenReturn(merchantsStats(5, 5, 5));

        when(queryRepository.getCardStats(anyString(), any(), any()))
                .thenReturn(cardStats(5, 10, 20_000.0, 500_000.0));

        when(queryRepository.getIpStats(anyString(), any()))
                .thenReturn(ipStats(3, 4));

        when(ipRequestFlagger.isFlagged("10.0.0.5")).thenReturn(false);

        TransactionsResponseDTO response = fraudEngineService.processTransaction(request);

        assertEquals("REVIEW", response.getStatus());
        assertTrue(response.getRiskScore() >= 50 && response.getRiskScore() < 70);
        assertEquals("Transaction flagged for manual review", response.getMessage());
    }

    @Test
    void hashCardNumber_whenInvalidLength_shouldThrowBadRequestException() {
        assertThrows(
                com.isw.fraudcheck.ErrorHandling.BadRequestException.class,
                () -> FraudEngineService.hashCardNumber("1234")
        );
    }

    @Test
    void hashCardNumber_whenValid_ShouldReturnHexString() {
        String hash = FraudEngineService.hashCardNumber("5399-8300-0000-0008");
        assertNotNull(hash);
        assertEquals(64, hash.length());

        assertTrue(hash.matches("[0-9a-f]+"));
    }
}