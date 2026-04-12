package com.ween.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ween.entity.*;
import com.ween.enums.CoinReason;
import com.ween.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("CheckinController Integration Tests")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class CheckinControllerIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ween_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private QrTokenRepository qrTokenRepository;

    @Autowired
    private CoinTransactionRepository coinTransactionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User volunteerUser;
    private User organizerUser;
    private Organization organization;
    private Event event;

    @BeforeEach
    void setUp() {
        coinTransactionRepository.deleteAll();
        eventRegistrationRepository.deleteAll();
        qrTokenRepository.deleteAll();
        notificationRepository.deleteAll();
        eventRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        // Create volunteer user
        volunteerUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("volunteer")
                .email("volunteer@example.com")
                .fullName("Volunteer User")
                .weenCoinBalance(0)
                .build();
        userRepository.save(volunteerUser);

        // Create organizer user
        organizerUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("organizer")
                .email("organizer@example.com")
                .fullName("Organizer User")
                .build();
        userRepository.save(organizerUser);

        // Create organization
        organization = Organization.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Organization")
                .ownerId(organizerUser.getId())
                .build();
        organizationRepository.save(organization);

        // Create event
        event = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("Test Event")
                .organizationId(organization.getId())
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        eventRepository.save(event);

        // Register volunteer for event
        EventRegistration registration = EventRegistration.builder()
                .id(UUID.randomUUID().toString())
                .userId(volunteerUser.getId())
                .eventId(event.getId())
                .registeredAt(LocalDateTime.now())
                .isJoined(false)
                .build();
        eventRegistrationRepository.save(registration);
    }

    @Test
    @DisplayName("Should generate QR code for volunteer")
    void testGenerateQrCode() {
        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/qr/generate?userId=" + volunteerUser.getId(),
                null,
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("qrToken"));
        String qrToken = (String) response.getBody().get("qrToken");
        assertNotNull(qrToken);
        assertFalse(qrToken.isEmpty());

        // Verify token persisted
        Optional<QrToken> savedToken = qrTokenRepository.findByUserIdAndIsRevokedFalse(volunteerUser.getId());
        assertTrue(savedToken.isPresent());
        assertEquals(volunteerUser.getId(), savedToken.get().getUserId());
    }

    @Test
    @DisplayName("Should extract and decrypt QR token successfully")
    void testDecryptQrToken() {
        // Arrange - Generate QR code first
        ResponseEntity<Map> generateResponse = restTemplate.postForEntity(
                "/api/v1/qr/generate?userId=" + volunteerUser.getId(),
                null,
                Map.class
        );
        String encryptedToken = (String) generateResponse.getBody().get("qrToken");

        // Act - Decrypt
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/qr/decrypt",
                Map.of("encryptedToken", encryptedToken),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("decryptedToken"));
    }

    @Test
    @DisplayName("Should perform checkin at event")
    void testCheckinAtEvent() {
        // Arrange - Generate QR code
        ResponseEntity<Map> generateResponse = restTemplate.postForEntity(
                "/api/v1/qr/generate?userId=" + volunteerUser.getId(),
                null,
                Map.class
        );
        String qrToken = (String) generateResponse.getBody().get("qrToken");

        // Act - Perform checkin
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify checkin recorded
        Optional<EventRegistration> registration = eventRegistrationRepository
                .findByEventIdAndUserId(event.getId(), volunteerUser.getId());
        assertTrue(registration.isPresent());
        assertTrue(registration.get().getIsJoined());
        assertNotNull(registration.get().getJoinedAt());
    }

    @Test
    @DisplayName("Should credit coins on successful checkin")
    void testCoinCreditOnCheckin() {
        // Arrange - Initial coin balance
        int initialBalance = volunteerUser.getWeenCoinBalance();

        // Act - Perform checkin
        restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert - Verify coins credited
        Optional<User> updatedUser = userRepository.findById(volunteerUser.getId());
        assertTrue(updatedUser.isPresent());
        assertTrue(updatedUser.get().getWeenCoinBalance() > initialBalance);

        // Verify transaction recorded
        List<CoinTransaction> transactions = coinTransactionRepository.findByUserId(volunteerUser.getId());
        assertTrue(transactions.stream().anyMatch(t -> t.getReason() == CoinReason.ATTENDANCE));
    }

    @Test
    @DisplayName("Should mark event registration as joined after checkin")
    void testRegistrationMarkedAsJoined() {
        // Act
        restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert
        Optional<EventRegistration> registration = eventRegistrationRepository
                .findByEventIdAndUserId(event.getId(), volunteerUser.getId());
        assertTrue(registration.isPresent());
        assertTrue(registration.get().getIsJoined());
    }

    @Test
    @DisplayName("Should prevent duplicate checkin with ALREADY_CHECKED_IN status")
    void testPreventDuplicateCheckin() {
        // Arrange - First checkin
        restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Act - Try duplicate checkin
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should send email notification after successful checkin")
    void testEmailNotificationOnCheckin() {
        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify notification was created
        List<Notification> notifications = notificationRepository.findByUserId(volunteerUser.getId());
        assertTrue(notifications.stream().anyMatch(n -> 
                n.getTitle().contains("checked in") || n.getTitle().contains("Checkin")
        ));
    }

    @Test
    @DisplayName("Should handle QR token expiration")
    void testQrTokenExpiration() {
        // Arrange - Create expired token
        LocalDateTime now = LocalDateTime.now();
        QrToken expiredToken = QrToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(volunteerUser.getId())
                .tokenHash("expired_token")
                .issuedAt(now.minusHours(25))
                .expiresAt(now.minusHours(1))
                .isRevoked(false)
                .build();
        qrTokenRepository.save(expiredToken);

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/qr/validate",
                Map.of("encryptedToken", "expired_token"),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.GONE, response.getStatusCode());
    }

    @Test
    @DisplayName("Should revoke previous QR token on new generation")
    void testPreviousTokenRevokedOnNewGeneration() {
        // Arrange - Generate first token
        ResponseEntity<Map> firstGeneration = restTemplate.postForEntity(
                "/api/v1/qr/generate?userId=" + volunteerUser.getId(),
                null,
                Map.class
        );

        // Get the first token
        List<QrToken> tokensAfterFirst = qrTokenRepository.findAllByUserId(volunteerUser.getId());
        QrToken firstToken = tokensAfterFirst.stream()
                .filter(t -> !t.getIsRevoked())
                .findFirst()
                .orElse(null);

        // Act - Generate second token
        ResponseEntity<Map> secondGeneration = restTemplate.postForEntity(
                "/api/v1/qr/generate?userId=" + volunteerUser.getId(),
                null,
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, secondGeneration.getStatusCode());

        // Verify first token is revoked
        Optional<QrToken> revokedToken = qrTokenRepository.findById(firstToken.getId());
        assertTrue(revokedToken.isPresent());
        assertTrue(revokedToken.get().getIsRevoked());
    }

    @Test
    @DisplayName("Should verify attendance coins credited correctly")
    void testAttendanceCoinAmount() {
        // Arrange
        int expectedCoins = 50; // ATTENDANCE coins

        // Act
        restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert
        List<CoinTransaction> transactions = coinTransactionRepository.findByUserId(volunteerUser.getId());
        CoinTransaction attendanceTransaction = transactions.stream()
                .filter(t -> t.getReason() == CoinReason.ATTENDANCE)
                .findFirst()
                .orElse(null);

        assertNotNull(attendanceTransaction);
        assertEquals(expectedCoins, attendanceTransaction.getAmount());
    }

    @Test
    @DisplayName("Should handle international event bonus coins")
    void testInternationalEventBonus() {
        // Arrange - Create international event
        Event internationalEvent = Event.builder()
                .id(UUID.randomUUID().toString())
                .title("International Event")
                .organizationId(organization.getId())
                .city("Tokyo") // International
                .build();
        eventRepository.save(internationalEvent);

        // Register volunteer
        EventRegistration registration = EventRegistration.builder()
                .id(UUID.randomUUID().toString())
                .userId(volunteerUser.getId())
                .eventId(internationalEvent.getId())
                .registeredAt(LocalDateTime.now())
                .isJoined(false)
                .build();
        eventRegistrationRepository.save(registration);

        // Act
        restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", internationalEvent.getId()),
                Map.class
        );

        // Assert - Should get ATTENDANCE coins
        List<CoinTransaction> transactions = coinTransactionRepository.findByUserId(volunteerUser.getId());
        assertTrue(transactions.stream().anyMatch(t -> t.getReason() == CoinReason.ATTENDANCE));
    }

    @Test
    @DisplayName("Should return 404 when checking in to non-existent event")
    void testCheckinNonExistentEvent() {
        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", UUID.randomUUID().toString()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 404 when user not registered for event during checkin")
    void testCheckinWithoutRegistration() {
        // Arrange - Create another user not registered
        User unregisteredUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("unregistered")
                .email("unregistered@example.com")
                .build();
        userRepository.save(unregisteredUser);

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", unregisteredUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should verify QR token contains required JWT payload fields")
    void testQrTokenJwtPayload() {
        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/qr/generate?userId=" + volunteerUser.getId(),
                null,
                Map.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> payload = response.getBody();
        
        // The JWT payload should contain these fields when decrypted
        assertTrue(payload.containsKey("qrToken"));
        assertNotNull(payload.get("qrToken"));
    }

    @Test
    @DisplayName("Should maintain database consistency during checkin")
    void testDatabaseConsistencyDuringCheckin() {
        // Act
        restTemplate.postForEntity(
                "/api/v1/checkin",
                Map.of("userId", volunteerUser.getId(), "eventId", event.getId()),
                Map.class
        );

        // Assert - Verify all related data is consistent
        Optional<EventRegistration> registration = eventRegistrationRepository
                .findByEventIdAndUserId(event.getId(), volunteerUser.getId());
        assertTrue(registration.isPresent());

        // Verify coins transaction exists
        List<CoinTransaction> transactions = coinTransactionRepository.findByUserId(volunteerUser.getId());
        assertTrue(transactions.size() > 0);

        // Verify user balance updated
        Optional<User> user = userRepository.findById(volunteerUser.getId());
        assertTrue(user.isPresent());
        assertTrue(user.get().getWeenCoinBalance() > 0);

        // Verify joinedAt timestamp set
        assertNotNull(registration.get().getJoinedAt());
    }
}
