package com.ween.service;

import com.ween.entity.EventRegistration;
import com.ween.entity.QrToken;
import com.ween.entity.User;
import com.ween.enums.CoinReason;
import com.ween.exception.QrTokenExpiredException;
import com.ween.exception.QrTokenInvalidException;
import com.ween.exception.ResourceNotFoundException;
import com.ween.repository.EventRegistrationRepository;
import com.ween.repository.QrTokenRepository;
import com.ween.repository.UserRepository;
import com.ween.security.AesUtil;
import com.ween.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QrService Unit Tests")
class QrServiceTest {

    @Mock
    private QrTokenRepository qrTokenRepository;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AesUtil aesUtil;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private CoinService coinService;

    @InjectMocks
    private QrService qrService;

    private User testUser;
    private String testUserId;
    private String testJwt;
    private String testEncryptedToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .weenCoinBalance(0)
                .build();
        testJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        testEncryptedToken = "encrypted_token_value";
    }

    @Test
    @DisplayName("Should generate QR token with valid JWT payload")
    void testGenerateQrToken() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(qrTokenRepository.findByUserIdAndIsRevokedFalse(testUserId)).thenReturn(Optional.empty());
        when(jwtUtil.generateRefreshToken(testUserId)).thenReturn(testJwt);
        when(aesUtil.encrypt(testJwt)).thenReturn(testEncryptedToken);
        QrToken qrToken = QrToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .tokenHash(testEncryptedToken)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isRevoked(false)
                .build();
        when(qrTokenRepository.save(any(QrToken.class))).thenReturn(qrToken);

        // Act
        String result = qrService.generateQrToken(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testEncryptedToken, result);
        verify(jwtUtil, times(1)).generateRefreshToken(testUserId);
        verify(aesUtil, times(1)).encrypt(testJwt);
        verify(qrTokenRepository, times(1)).save(any(QrToken.class));
    }

    @Test
    @DisplayName("Should revoke existing token before generating new one")
    void testRevokeExistingTokenBeforeGenerating() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        QrToken existingToken = QrToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .tokenHash("old_token")
                .issuedAt(now.minusHours(12))
                .expiresAt(now.plusHours(12))
                .isRevoked(false)
                .build();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(qrTokenRepository.findByUserIdAndIsRevokedFalse(testUserId)).thenReturn(Optional.of(existingToken));
        when(jwtUtil.generateRefreshToken(testUserId)).thenReturn(testJwt);
        when(aesUtil.encrypt(testJwt)).thenReturn(testEncryptedToken);
        QrToken newToken = QrToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .tokenHash(testEncryptedToken)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isRevoked(false)
                .build();
        when(qrTokenRepository.save(any(QrToken.class))).thenReturn(newToken);

        // Act
        qrService.generateQrToken(testUserId);

        // Assert
        assertTrue(existingToken.getIsRevoked());
        verify(qrTokenRepository, times(2)).save(any(QrToken.class));
    }

    @Test
    @DisplayName("Should throw exception when generating QR for non-existent user")
    void testGenerateQrForNonExistentUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                qrService.generateQrToken(testUserId)
        );
    }

    @Test
    @DisplayName("Should get QR token successfully")
    void testGetQrToken() {
        // Arrange
        when(qrTokenRepository.findByUserIdAndIsRevokedFalse(testUserId))
                .thenReturn(Optional.of(QrToken.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(testUserId)
                        .tokenHash(testEncryptedToken)
                        .isRevoked(false)
                        .build()));

        // Act
        String result = qrService.getQrToken(testUserId);

        // Assert
        assertEquals(testEncryptedToken, result);
    }

    @Test
    @DisplayName("Should throw exception when QR token not found")
    void testGetQrTokenNotFound() {
        // Arrange
        when(qrTokenRepository.findByUserIdAndIsRevokedFalse(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                qrService.getQrToken(testUserId)
        );
    }

    @Test
    @DisplayName("Should decrypt QR token successfully")
    void testDecryptQrToken() {
        // Arrange
        when(aesUtil.decrypt(testEncryptedToken)).thenReturn(testJwt);

        // Act
        String result = qrService.validateAndDecryptQrToken(testEncryptedToken);

        // Assert
        assertEquals(testJwt, result);
        verify(aesUtil, times(1)).decrypt(testEncryptedToken);
    }

    @Test
    @DisplayName("Should throw exception on invalid encrypted token")
    void testDecryptInvalidToken() {
        // Arrange
        when(aesUtil.decrypt(testEncryptedToken)).thenThrow(new RuntimeException("Decryption failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                qrService.validateAndDecryptQrToken(testEncryptedToken)
        );
    }

    @Test
    @DisplayName("Should perform successful checkin with valid token")
    void testCheckinWithValidToken() {
        // Arrange
        String eventId = UUID.randomUUID().toString();
        EventRegistration registration = EventRegistration.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(eventId)
                .isJoined(false)
                .build();
        when(eventRegistrationRepository.findByEventIdAndUserId(eventId, testUserId))
                .thenReturn(Optional.of(registration));
        when(coinService.credit(testUserId, 50, CoinReason.ATTENDANCE, eventId))
                .thenReturn(null);

        // Act
        qrService.checkin(testUserId, eventId);

        // Assert
        assertTrue(registration.getIsJoined());
        assertNotNull(registration.getJoinedAt());
        verify(eventRegistrationRepository, times(1)).save(registration);
        verify(coinService, times(1)).credit(testUserId, 50, CoinReason.ATTENDANCE, eventId);
    }

    @Test
    @DisplayName("Should prevent checkin if already checked in")
    void testPreventDuplicateCheckin() {
        // Arrange
        String eventId = UUID.randomUUID().toString();
        EventRegistration registration = EventRegistration.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(eventId)
                .isJoined(true)
                .joinedAt(LocalDateTime.now())
                .build();
        when(eventRegistrationRepository.findByEventIdAndUserId(eventId, testUserId))
                .thenReturn(Optional.of(registration));

        // Act & Assert
        assertThrows(Exception.class, () ->
                qrService.checkin(testUserId, eventId)
        );
        verify(eventRegistrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user not registered for event during checkin")
    void testCheckinWithoutRegistration() {
        // Arrange
        String eventId = UUID.randomUUID().toString();
        when(eventRegistrationRepository.findByEventIdAndUserId(eventId, testUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () ->
                qrService.checkin(testUserId, eventId)
        );
    }

    @Test
    @DisplayName("Should credit coins on international event attendance")
    void testCoinCreditOnInternationalEventAttendance() {
        // Arrange
        String eventId = UUID.randomUUID().toString();
        EventRegistration registration = EventRegistration.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(eventId)
                .isJoined(false)
                .build();
        when(eventRegistrationRepository.findByEventIdAndUserId(eventId, testUserId))
                .thenReturn(Optional.of(registration));
        when(coinService.credit(testUserId, 50, CoinReason.ATTENDANCE, eventId))
                .thenReturn(null);

        // Act
        qrService.checkin(testUserId, eventId);

        // Assert
        verify(coinService, times(1)).credit(testUserId, 50, CoinReason.ATTENDANCE, eventId);
    }

    @Test
    @DisplayName("Should validate QR token has required JWT fields")
    void testQrTokenContainsRequiredFields() {
        // Arrange
        String jwtWithFields = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJ1c2VySWQiOiJ1c2VyMTIzIiwiZW1haWxIYXNoIjoiYWJjZGVmIiwiaWF0IjoxNjQyNDU2OTAwLCJleHAiOjE2NDI1NDMzMDAsInBsYXRmb3JtIjoibW9iaWxlIn0." +
                "signature";
        when(aesUtil.decrypt(testEncryptedToken)).thenReturn(jwtWithFields);

        // Act
        String result = qrService.validateAndDecryptQrToken(testEncryptedToken);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("userId"));
        assertTrue(result.contains("emailHash"));
        assertTrue(result.contains("iat"));
        assertTrue(result.contains("exp"));
        assertTrue(result.contains("platform"));
    }

    @Test
    @DisplayName("Should set correct token expiry of 24 hours")
    void testTokenExpirySet24Hours() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(qrTokenRepository.findByUserIdAndIsRevokedFalse(testUserId)).thenReturn(Optional.empty());
        when(jwtUtil.generateRefreshToken(testUserId)).thenReturn(testJwt);
        when(aesUtil.encrypt(testJwt)).thenReturn(testEncryptedToken);
        LocalDateTime beforeCall = LocalDateTime.now();
        QrToken qrToken = QrToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .tokenHash(testEncryptedToken)
                .issuedAt(beforeCall)
                .expiresAt(beforeCall.plusHours(24))
                .isRevoked(false)
                .build();
        when(qrTokenRepository.save(any(QrToken.class))).thenReturn(qrToken);

        // Act
        qrService.generateQrToken(testUserId);

        // Assert
        verify(qrTokenRepository, times(1)).save(argThat(token ->
                token.getExpiresAt().isAfter(beforeCall.plusHours(23)) &&
                        token.getExpiresAt().isBefore(beforeCall.plusHours(25))
        ));
    }
}
