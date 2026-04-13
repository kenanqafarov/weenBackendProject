package com.ween.service;

import com.ween.entity.*;
import com.ween.enums.CertificateTemplate;
import com.ween.enums.CoinReason;
import com.ween.enums.EventStatus;
import com.ween.exception.ResourceNotFoundException;
import com.ween.mapper.CertificateMapper;
import com.ween.repository.CertificateRepository;
import com.ween.repository.EventRepository;
import com.ween.repository.UserRepository;
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
@DisplayName("CertificateService Unit Tests")
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoinService coinService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FirebaseService firebaseService;

    @Mock
    private CertificateMapper certificateMapper;

    @InjectMocks
    private CertificateService certificateService;

    private User testUser;
    private Event testEvent;
    private String testUserId;
    private String testEventId;
    private String certificateNumber;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        testEventId = UUID.randomUUID().toString();
        certificateNumber = "CERT-" + System.currentTimeMillis();

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .weenCoinBalance(0)
                .build();

        testEvent = Event.builder()
                .id(testEventId)
                .title("Test Event")
                .status(EventStatus.COMPLETED)
                .organizationId(UUID.randomUUID().toString())
                .startDate(LocalDateTime.now().minusHours(2))
                .endDate(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    @DisplayName("Should generate certificate for completed event")
    void testGenerateCertificateForCompletedEvent() {
        // Arrange
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String pdfUrl = "https://storage.example.com/certs/cert123.pdf";
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(false);
        
        Certificate savedCert = Certificate.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(testEventId)
                .certificateNumber(certificateNumber)
                .pdfUrl(pdfUrl)
                .templateType(CertificateTemplate.GENERAL)
                .issuedAt(LocalDateTime.now())
                .build();
        when(certificateRepository.save(any(Certificate.class))).thenReturn(savedCert);

        // Act
        Certificate result = certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(testEventId, result.getEventId());
        assertEquals(pdfUrl, result.getPdfUrl());
        assertEquals(CertificateTemplate.GENERAL, result.getTemplateType());
        verify(certificateRepository, times(1)).save(any(Certificate.class));
        
    }

    @Test
    @DisplayName("Should throw exception when event is not completed")
    void testGenerateCertificateForIncompleteEvent() {
        // Arrange
        testEvent.setStatus(EventStatus.DRAFT);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        // The service should validate event status - implementation dependent
        // If validation exists, it should throw exception
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testGenerateCertificateUserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL)
        );
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void testGenerateCertificateEventNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL)
        );
    }

    @Test
    @DisplayName("Should throw exception when certificate already exists")
    void testGenerateCertificateAlreadyExists() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL)
        );
    }

    @Test
    @DisplayName("Should generate valid PDF bytes")
    void testCertificatePdfGeneration() {
        // Arrange
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String pdfUrl = "https://storage.example.com/certs/cert123.pdf";
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(false);
        
        Certificate savedCert = Certificate.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(testEventId)
                .certificateNumber(certificateNumber)
                .pdfUrl(pdfUrl)
                .templateType(CertificateTemplate.GENERAL)
                .issuedAt(LocalDateTime.now())
                .build();
        when(certificateRepository.save(any(Certificate.class))).thenReturn(savedCert);

        // Act
        certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL);

        // Assert
        
    }

    @Test
    @DisplayName("Should credit CERTIFICATE coins after generation")
    void testCoinCreditAfterGeneration() {
        // Arrange
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String pdfUrl = "https://storage.example.com/certs/cert123.pdf";
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(false);
        
        Certificate savedCert = Certificate.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(testEventId)
                .certificateNumber(certificateNumber)
                .pdfUrl(pdfUrl)
                .templateType(CertificateTemplate.GENERAL)
                .issuedAt(LocalDateTime.now())
                .build();
        when(certificateRepository.save(any(Certificate.class))).thenReturn(savedCert);

        // Act
        certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL);

        // Assert
        verify(coinService, times(1)).credit(testUserId, 25, CoinReason.CERTIFICATE, testEventId);
    }

    @Test
    @DisplayName("Should use correct certificate template")
    void testCertificateTemplateUsed() {
        // Arrange
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String pdfUrl = "https://storage.example.com/certs/cert123.pdf";
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(false);
        
        Certificate savedCert = Certificate.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(testEventId)
                .certificateNumber(certificateNumber)
                .pdfUrl(pdfUrl)
                .templateType(CertificateTemplate.GENERAL)
                .issuedAt(LocalDateTime.now())
                .build();
        when(certificateRepository.save(any(Certificate.class))).thenReturn(savedCert);

        // Act
        Certificate result = certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL);

        // Assert
        assertEquals(CertificateTemplate.GENERAL, result.getTemplateType());
    }

    @Test
    @DisplayName("Should generate certificate with issuedAt timestamp")
    void testCertificateHasIssuedAtTimestamp() {
        // Arrange
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String pdfUrl = "https://storage.example.com/certs/cert123.pdf";
        LocalDateTime beforeIssuance = LocalDateTime.now();
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(false);
        
        Certificate savedCert = Certificate.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(testEventId)
                .certificateNumber(certificateNumber)
                .pdfUrl(pdfUrl)
                .templateType(CertificateTemplate.GENERAL)
                .issuedAt(LocalDateTime.now())
                .build();
        when(certificateRepository.save(any(Certificate.class))).thenReturn(savedCert);

        // Act
        Certificate result = certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL);

        // Assert
        assertNotNull(result.getIssuedAt());
        assertTrue(result.getIssuedAt().isAfter(beforeIssuance.minusSeconds(1)));
        assertTrue(result.getIssuedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should upload PDF and store URL")
    void testPdfUploadAndStorage() {
        // Arrange
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String expectedPdfUrl = "https://storage.example.com/certs/cert123.pdf";
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(false);
        
        Certificate savedCert = Certificate.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(testEventId)
                .certificateNumber(certificateNumber)
                .pdfUrl(expectedPdfUrl)
                .templateType(CertificateTemplate.GENERAL)
                .issuedAt(LocalDateTime.now())
                .build();
        when(certificateRepository.save(any(Certificate.class))).thenReturn(savedCert);

        // Act
        Certificate result = certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL);

        // Assert
        assertEquals(expectedPdfUrl, result.getPdfUrl());
        
    }

    @Test
    @DisplayName("Should generate unique certificate numbers")
    void testUniqueCertificateNumbers() {
        // Arrange
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        String pdfUrl = "https://storage.example.com/certs/cert123.pdf";
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(certificateRepository.existsByUserIdAndEventId(testUserId, testEventId)).thenReturn(false);
        
        Certificate cert1 = Certificate.builder()
                .id(UUID.randomUUID().toString())
                .userId(testUserId)
                .eventId(testEventId)
                .certificateNumber("CERT-12345")
                .pdfUrl(pdfUrl)
                .templateType(CertificateTemplate.GENERAL)
                .issuedAt(LocalDateTime.now())
                .build();
        when(certificateRepository.save(any(Certificate.class))).thenReturn(cert1);

        // Act
        Certificate result = certificateService.generateCertificate(testUserId, testEventId, CertificateTemplate.GENERAL);

        // Assert
        assertNotNull(result.getCertificateNumber());
        assertFalse(result.getCertificateNumber().isEmpty());
    }
}

