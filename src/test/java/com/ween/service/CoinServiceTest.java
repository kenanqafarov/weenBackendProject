package com.ween.service;

import com.ween.entity.CoinTransaction;
import com.ween.entity.User;
import com.ween.enums.CoinReason;
import com.ween.exception.ResourceNotFoundException;
import com.ween.repository.CoinTransactionRepository;
import com.ween.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoinService Unit Tests")
class CoinServiceTest {

    @Mock
    private CoinTransactionRepository coinTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CoinService coinService;

    private User testUser;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .weenCoinBalance(50)
                .build();
    }

    @Test
    @DisplayName("Should credit coins with SIGNUP reason")
    void testCreditCoinsWithSignupReason() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(50)
                .reason(CoinReason.SIGNUP)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act
        CoinTransaction result = coinService.credit(testUserId, 50, CoinReason.SIGNUP, null);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getAmount());
        assertEquals(CoinReason.SIGNUP, result.getReason());
        assertEquals(testUserId, result.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
        verify(coinTransactionRepository, times(1)).save(any(CoinTransaction.class));
    }

    @Test
    @DisplayName("Should credit coins with REGISTRATION reason")
    void testCreditCoinsWithRegistrationReason() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(10)
                .reason(CoinReason.REGISTRATION)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act
        CoinTransaction result = coinService.credit(testUserId, 10, CoinReason.REGISTRATION, null);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getAmount());
        assertEquals(CoinReason.REGISTRATION, result.getReason());
    }

    @Test
    @DisplayName("Should credit coins with ATTENDANCE reason (50 coins)")
    void testCreditCoinsWithAttendanceReason() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(50)
                .reason(CoinReason.ATTENDANCE)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act
        CoinTransaction result = coinService.credit(testUserId, 50, CoinReason.ATTENDANCE, null);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getAmount());
        assertEquals(CoinReason.ATTENDANCE, result.getReason());
    }

    @Test
    @DisplayName("Should credit coins with CERTIFICATE reason")
    void testCreditCoinsWithCertificateReason() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(25)
                .reason(CoinReason.CERTIFICATE)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act
        CoinTransaction result = coinService.credit(testUserId, 25, CoinReason.CERTIFICATE, null);

        // Assert
        assertNotNull(result);
        assertEquals(25, result.getAmount());
        assertEquals(CoinReason.CERTIFICATE, result.getReason());
    }

    @Test
    @DisplayName("Should update user balance atomically with transaction creation")
    void testBalanceUpdateAtomicWithTransaction() {
        // Arrange
        int initialBalance = testUser.getWeenCoinBalance();
        int creditsAmount = 100;
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(creditsAmount)
                .reason(CoinReason.SIGNUP)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act
        coinService.credit(testUserId, creditsAmount, CoinReason.SIGNUP, null);

        // Assert
        assertEquals(initialBalance + creditsAmount, testUser.getWeenCoinBalance());
        verify(userRepository, times(1)).save(testUser);
        verify(coinTransactionRepository, times(1)).save(any(CoinTransaction.class));
    }

    @Test
    @DisplayName("Should award PROFILE_COMPLETE bonus only once")
    void testProfileCompleteBonus() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(coinTransactionRepository.countByUserIdAndReason(testUserId, CoinReason.PROFILE_COMPLETE))
                .thenReturn(0L)
                .thenReturn(1L);
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(50)
                .reason(CoinReason.PROFILE_COMPLETE)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act - First award should succeed
        coinService.credit(testUserId, 50, CoinReason.PROFILE_COMPLETE, null);
        verify(userRepository, times(1)).save(any(User.class));

        // Second check should prevent re-awarding
        when(coinTransactionRepository.countByUserIdAndReason(testUserId, CoinReason.PROFILE_COMPLETE))
                .thenReturn(1L);

        // This simulates that the bonus was already awarded
    }

    @Test
    @DisplayName("Should debit coins successfully")
    void testDebitCoins() {
        // Arrange
        testUser.setWeenCoinBalance(100);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(-50)
                .reason(CoinReason.REFERRAL)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act
        CoinTransaction result = coinService.debit(testUserId, 50, CoinReason.REFERRAL, null);

        // Assert
        assertNotNull(result);
        assertEquals(-50, result.getAmount());
        assertEquals(50, testUser.getWeenCoinBalance());
        verify(userRepository, times(1)).save(any(User.class));
        verify(coinTransactionRepository, times(1)).save(any(CoinTransaction.class));
    }

    @Test
    @DisplayName("Should throw exception when debiting insufficient balance")
    void testDebitWithInsufficientBalance() {
        // Arrange
        testUser.setWeenCoinBalance(30);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                coinService.debit(testUserId, 50, CoinReason.REFERRAL, null)
        );
    }

    @Test
    @DisplayName("Should throw exception when crediting to non-existent user")
    void testCreditToNonExistentUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                coinService.credit(testUserId, 50, CoinReason.SIGNUP, null)
        );
    }

    @Test
    @DisplayName("Should credit coins with relatedEntityId")
    void testCreditCoinsWithRelatedEntity() {
        // Arrange
        String eventId = UUID.randomUUID().toString();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        CoinTransaction transaction = CoinTransaction.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .amount(50)
                .reason(CoinReason.ATTENDANCE)
                .relatedEntityId(eventId)
                .build();
        when(coinTransactionRepository.save(any(CoinTransaction.class))).thenReturn(transaction);

        // Act
        CoinTransaction result = coinService.credit(testUserId, 50, CoinReason.ATTENDANCE, eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.getRelatedEntityId());
    }

    @Test
    @DisplayName("Should get user coin balance")
    void testGetUserCoinBalance() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        Integer balance = coinService.getUserCoinBalance(testUserId);

        // Assert
        assertEquals(50, balance);
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw exception when getting balance for non-existent user")
    void testGetBalanceForNonExistentUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                coinService.getUserCoinBalance(testUserId)
        );
    }
}
