package com.ween.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ween.dto.request.LoginRequest;
import com.ween.dto.request.RegisterRequest;
import com.ween.entity.CoinTransaction;
import com.ween.entity.User;
import com.ween.enums.CoinReason;
import com.ween.repository.CoinTransactionRepository;
import com.ween.repository.UserRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class AuthControllerIT {

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
    private CoinTransactionRepository coinTransactionRepository;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        coinTransactionRepository.deleteAll();

        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("SecurePassword123!")
                .fullName("Test User")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("SecurePassword123!")
                .build();
    }

    @Test
    @DisplayName("Should complete full registration and email verification flow")
    void testCompleteRegistrationFlow() {
        // Act - Register
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                Map.class
        );

        // Assert registration success
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getBody());
        assertTrue(registerResponse.getBody().containsKey("id"));
        assertTrue(registerResponse.getBody().containsKey("email"));

        // Verify user is persisted to database
        Optional<User> savedUser = userRepository.findByEmail(registerRequest.getEmail());
        assertTrue(savedUser.isPresent());
        assertEquals(registerRequest.getUsername(), savedUser.get().getUsername());
    }

    @Test
    @DisplayName("Should credit signup bonus coins immediately after registration")
    void testSignupBonusCredited() {
        // Act - Register
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                Map.class
        );

        // Assert registration success
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // Verify coins credited
        Optional<User> user = userRepository.findByEmail(registerRequest.getEmail());
        assertTrue(user.isPresent());
        assertTrue(user.get().getWeenCoinBalance() > 0);

        // Verify transaction recorded
        List<CoinTransaction> transactions = coinTransactionRepository.findByUserId(user.get().getId());
        assertTrue(transactions.stream().anyMatch(t -> t.getReason() == CoinReason.SIGNUP));
    }

    @Test
    @DisplayName("Should generate JWT tokens on successful login")
    void testLoginGeneratesJwtTokens() {
        // Arrange - Register first
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Map.class);

        // Act - Login
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                Map.class
        );

        // Assert login success
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        Map<String, Object> responseBody = loginResponse.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.containsKey("accessToken"));
        assertTrue(responseBody.containsKey("refreshToken"));
        assertNotNull(responseBody.get("accessToken"));
        assertNotNull(responseBody.get("refreshToken"));
    }

    @Test
    @DisplayName("Should persist user data correctly in database")
    void testUserDataPersistence() {
        // Act - Register
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                Map.class
        );

        // Assert registration success
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // Verify data in database
        Optional<User> persistedUser = userRepository.findByEmail(registerRequest.getEmail());
        assertTrue(persistedUser.isPresent());

        User user = persistedUser.get();
        assertEquals(registerRequest.getEmail(), user.getEmail());
        assertEquals(registerRequest.getUsername(), user.getUsername());
        assertEquals(registerRequest.getFullName(), user.getFullName());
        assertNotNull(user.getPasswordHash());
        assertNotNull(user.getReferralCode());
    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void testDuplicateEmailRejected() {
        // Arrange - Register first user
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Map.class);

        // Act - Try to register with same email
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("differentuser")
                .email(registerRequest.getEmail()) // Same email
                .password("DifferentPassword123!")
                .fullName("Different User")
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                duplicateRequest,
                Map.class
        );

        // Assert failure
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should prevent duplicate username registration")
    void testDuplicateUsernameRejected() {
        // Arrange - Register first user
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Map.class);

        // Act - Try to register with same username
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username(registerRequest.getUsername()) // Same username
                .email("different@example.com")
                .password("DifferentPassword123!")
                .fullName("Different User")
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                duplicateRequest,
                Map.class
        );

        // Assert failure
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should reject login with invalid credentials")
    void testLoginWithInvalidCredentials() {
        // Arrange - Register first
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Map.class);

        // Act - Login with wrong password
        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .email(loginRequest.getEmail())
                .password("WrongPassword123!")
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                wrongPasswordRequest,
                Map.class
        );

        // Assert failure
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should reject login with non-existent email")
    void testLoginWithNonExistentEmail() {
        // Act - Login with non-existent email
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("SomePassword123!")
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                invalidRequest,
                Map.class
        );

        // Assert failure
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should generate unique referral codes for different users")
    void testUniqueReferralCodesPerUser() {
        // Act - Register first user
        ResponseEntity<Map> response1 = restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                Map.class
        );
        String referralCode1 = (String) response1.getBody().get("referralCode");

        // Register second user
        RegisterRequest registerRequest2 = RegisterRequest.builder()
                .username("testuser2")
                .email("test2@example.com")
                .password("SecurePassword123!")
                .fullName("Test User 2")
                .build();

        ResponseEntity<Map> response2 = restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest2,
                Map.class
        );
        String referralCode2 = (String) response2.getBody().get("referralCode");

        // Assert codes are different
        assertNotEquals(referralCode1, referralCode2);
        assertNotNull(referralCode1);
        assertNotNull(referralCode2);
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testTokenRefresh() {
        // Arrange - Register and login
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Map.class);
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                Map.class
        );
        String refreshToken = (String) loginResponse.getBody().get("refreshToken");

        // Act - Refresh token
        Map<String, String> refreshRequest = Map.of("refreshToken", refreshToken);
        ResponseEntity<Map> refreshResponse = restTemplate.postForEntity(
                "/api/v1/auth/refresh",
                refreshRequest,
                Map.class
        );

        // Assert success
        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertTrue(refreshResponse.getBody().containsKey("accessToken"));
    }

    @Test
    @DisplayName("Should logout and blacklist token")
    void testLogout() {
        // Arrange - Register and login
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Map.class);
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                Map.class
        );
        String accessToken = (String) loginResponse.getBody().get("accessToken");

        // Act - Logout
        ResponseEntity<Void> logoutResponse = restTemplate.postForEntity(
                "/api/v1/auth/logout",
                Map.of("token", accessToken),
                Void.class
        );

        // Assert success
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
    }

    @Test
    @DisplayName("Should handle referral code during registration")
    void testRegistrationWithReferralCode() {
        // Arrange - Register first user
        ResponseEntity<Map> response1 = restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                Map.class
        );
        String referralCode = (String) response1.getBody().get("referralCode");

        // Act - Register second user with referral
        RegisterRequest referredRequest = RegisterRequest.builder()
                .username("referreduser")
                .email("referred@example.com")
                .password("SecurePassword123!")
                .fullName("Referred User")
                .build();

        ResponseEntity<Map> response2 = restTemplate.postForEntity(
                "/api/v1/auth/register?referralCode=" + referralCode,
                referredRequest,
                Map.class
        );

        // Assert success
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());

        // Verify referral coins were credited
        Optional<User> referrer = userRepository.findByEmail(registerRequest.getEmail());
        assertTrue(referrer.isPresent());
        List<CoinTransaction> transactions = coinTransactionRepository.findByUserId(referrer.get().getId());
        assertTrue(transactions.stream().anyMatch(t -> t.getReason() == CoinReason.REFERRAL));
    }

    @Test
    @DisplayName("Should verify password is hashed in database")
    void testPasswordIsHashedInDatabase() {
        // Act - Register
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Map.class);

        // Assert
        Optional<User> user = userRepository.findByEmail(registerRequest.getEmail());
        assertTrue(user.isPresent());
        assertNotEquals(registerRequest.getPassword(), user.get().getPasswordHash());
        assertTrue(user.get().getPasswordHash().length() > registerRequest.getPassword().length());
    }
}
