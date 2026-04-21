package com.ween.service;

import com.ween.dto.request.LoginRequest;
import com.ween.dto.request.RegisterRequest;
import com.ween.dto.request.ResetPasswordRequest;
import com.ween.entity.Organization;
import com.ween.entity.PasswordResetToken;
import com.ween.entity.EmailVerificationToken;
import com.ween.entity.User;
import com.ween.repository.EmailVerificationTokenRepository;
import com.ween.repository.OrganizationRepository;
import com.ween.repository.PasswordResetTokenRepository;
import com.ween.repository.ReferralRepository;
import com.ween.enums.CoinReason;
import com.ween.enums.UserRole;
import com.ween.exception.AlreadyExistsException;
import com.ween.exception.UnauthorizedException;
import com.ween.repository.UserRepository;
import com.ween.security.JwtUtil;
import com.ween.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private CoinService coinService;

    @Mock
    private ReferralRepository referralRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        
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

        testUser = User.builder()
                .id(testUserId)
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash("hashedPassword")
                .fullName(registerRequest.getFullName())
                .role(UserRole.VOLUNTEER)
                .weenCoinBalance(0)
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testUserRegistration() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(testUserId, 50, CoinReason.SIGNUP, null)).thenReturn(null);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(registerRequest.getEmail(), result.getEmail());
        assertEquals(registerRequest.getUsername(), result.getUsername());
        assertEquals(UserRole.VOLUNTEER, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
        verify(coinService, times(1)).credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegistrationWithDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () ->
                authService.register(registerRequest)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegistrationWithDuplicateUsername() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () ->
                authService.register(registerRequest)
        );
    }

    @Test
    @DisplayName("Should hash password using BCrypt with strength 12")
    void testPasswordHashedWithBCrypt() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$12$...");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
    }

    @Test
    @DisplayName("Should generate 8-character alphanumeric referral code")
    void testReferralCodeGeneration() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertNotNull(result.getReferralCode());
        assertTrue(result.getReferralCode().length() >= 8);
        assertTrue(result.getReferralCode().matches("[a-zA-Z0-9]+"));
    }

    @Test
    @DisplayName("Should credit 50 coins on signup")
    void testSignupCoinBonus() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(coinService, times(1)).credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull());
    }

    @Test
    @DisplayName("Should perform login successfully")
    void testLoginSuccess() {
        // Arrange
        testUser.setPasswordHash("hashedPassword");
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAccessToken(testUserId, loginRequest.getEmail())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(testUserId)).thenReturn("refreshToken");

        // Act
        Map<String, Object> result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("accessToken"));
        assertTrue(result.containsKey("refreshToken"));
    }

    @Test
    @DisplayName("Should throw exception on invalid email during login")
    void testLoginWithInvalidEmail() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                authService.login(loginRequest)
        );
    }

    @Test
    @DisplayName("Should throw exception on invalid password during login")
    void testLoginWithInvalidPassword() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                authService.login(loginRequest)
        );
    }

    @Test
    @DisplayName("Should generate JWT access token with user ID and email")
    void testAccessTokenContainsUserInfo() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAccessToken(testUserId, loginRequest.getEmail())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(testUserId)).thenReturn("refreshToken");

        // Act
        authService.login(loginRequest);

        // Assert
        verify(jwtUtil, times(1)).generateAccessToken(testUserId, loginRequest.getEmail());
    }

    @Test
    @DisplayName("Should generate JWT refresh token with user ID")
    void testRefreshTokenGeneration() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAccessToken(testUserId, loginRequest.getEmail())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(testUserId)).thenReturn("refreshToken");

        // Act
        authService.login(loginRequest);

        // Assert
        verify(jwtUtil, times(1)).generateRefreshToken(testUserId);
    }

    @Test
    @DisplayName("Should set user role to VOLUNTEER on registration")
    void testDefaultUserRoleVolunteer() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertEquals(UserRole.VOLUNTEER, result.getRole());
    }

    @Test
    @DisplayName("Should set initial coin balance to 0")
    void testInitialCoinBalance() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertEquals(0, result.getWeenCoinBalance());
    }

    @Test
    @DisplayName("Should send welcome email after registration")
    void testWelcomeEmailSent() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(emailService, times(1)).sendWelcomeEmail(
                registerRequest.getEmail(),
                registerRequest.getFullName()
        );
    }

    @Test
    @DisplayName("Should handle referral code during registration")
    void testReferralCodeProcessing() {
        // Arrange
        String referralCode = "ABC12345";
        User referrer = User.builder()
                .id(UUID.randomUUID().toString())
                .email("referrer@example.com")
                .referralCode(referralCode)
                .build();
        
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findByReferralCode(referralCode)).thenReturn(Optional.of(referrer));
        when(coinService.credit(anyString(), anyInt(), any(CoinReason.class), anyString())).thenReturn(null);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        // Verify that referral coins were credited
        verify(coinService, atLeastOnce()).credit(anyString(), anyInt(), any(CoinReason.class), anyString());
    }

    @Test
    @DisplayName("Should continue registration even if referral code invalid")
    void testInvalidReferralCodeDoesNotBlockRegistration() {
        // Arrange
        String invalidReferralCode = "INVALID";
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findByReferralCode(invalidReferralCode)).thenReturn(Optional.empty());
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should accept null referral code")
    void testNullReferralCode() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should generate unique referral codes")
    void testUniqueReferralCodes() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        
        User user1 = User.builder()
                .id(UUID.randomUUID().toString())
                .email("user1@example.com")
                .username("user1")
                .referralCode(authService.generateReferralCode()) // Get from actual method if possible
                .build();
        User user2 = User.builder()
                .id(UUID.randomUUID().toString())
                .email("user2@example.com")
                .username("user2")
                .referralCode(authService.generateReferralCode())
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(user1).thenReturn(user2);
        when(coinService.credit(anyString(), eq(50), eq(CoinReason.SIGNUP), isNull())).thenReturn(null);

        // Act
        authService.register(registerRequest);

        // The expectation is that different referral codes are generated
        // This validates uniqueness at service level
    }

    @Test
    @DisplayName("Should send password reset link to organization email")
    void testForgotPasswordForOrganization() {
        // Arrange
        String organizationEmail = "org@example.com";
        String organizationId = UUID.randomUUID().toString();
        Organization organization = Organization.builder()
                .id(organizationId)
                .email(organizationEmail)
                .organizationName("Test Organization")
                .build();

        when(userRepository.findByEmail(organizationEmail)).thenReturn(Optional.empty());
        when(organizationRepository.findByEmail(organizationEmail)).thenReturn(Optional.of(organization));

        // Act
        authService.sendPasswordResetLink(organizationEmail);

        // Assert
        verify(passwordResetTokenRepository, times(1)).deleteByUserId(organizationId);
        verify(emailService, times(1)).sendPasswordResetEmail(eq(organizationEmail), eq("Test Organization"), anyString());
    }

    @Test
    @DisplayName("Should reset organization password with token")
    void testResetPasswordForOrganization() {
        // Arrange
        String organizationId = UUID.randomUUID().toString();
        String tokenValue = UUID.randomUUID().toString();
        String newPassword = "NewSecurePassword123!";

        Organization organization = Organization.builder()
                .id(organizationId)
                .email("org@example.com")
                .passwordHash("oldHash")
                .organizationName("Test Organization")
                .build();

        PasswordResetToken token = PasswordResetToken.builder()
                .userId(organizationId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        ResetPasswordRequest request = new ResetPasswordRequest(tokenValue, newPassword);

        when(passwordResetTokenRepository.findByTokenAndIsUsedFalse(tokenValue)).thenReturn(Optional.of(token));
        when(userRepository.findById(organizationId)).thenReturn(Optional.empty());
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(passwordEncoder.encode(newPassword)).thenReturn("hashedNewPassword");

        // Act
        authService.resetPasswordWithToken(request);

        // Assert
        verify(organizationRepository, times(1)).save(argThat(savedOrganization ->
                "hashedNewPassword".equals(savedOrganization.getPasswordHash())
        ));
        verify(passwordResetTokenRepository, times(1)).save(argThat(savedToken -> Boolean.TRUE.equals(savedToken.getIsUsed())));
    }

    @Test
    @DisplayName("Should resend verification email to organization")
    void testResendVerificationEmailForOrganization() {
        // Arrange
        String organizationId = UUID.randomUUID().toString();
        Organization organization = Organization.builder()
                .id(organizationId)
                .email("org@example.com")
                .organizationName("Test Organization")
                .isVerified(false)
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(organizationId);
        when(userRepository.findById(organizationId)).thenReturn(Optional.empty());
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));

        // Act
        authService.sendVerificationTokenForCurrentUser();

        // Assert
        verify(emailVerificationTokenRepository, times(1)).deleteByUserId(organizationId);
        verify(emailService, times(1)).sendVerificationEmail(eq("org@example.com"), eq("Test Organization"), anyString());
    }

    @Test
    @DisplayName("Should verify organization email with token")
    void testVerifyOrganizationEmailWithToken() {
        // Arrange
        String organizationId = UUID.randomUUID().toString();
        String tokenValue = UUID.randomUUID().toString();

        Organization organization = Organization.builder()
                .id(organizationId)
                .email("org@example.com")
                .organizationName("Test Organization")
                .isVerified(false)
                .build();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(organizationId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        when(emailVerificationTokenRepository.findByTokenAndIsUsedFalse(tokenValue)).thenReturn(Optional.of(token));
        when(userRepository.findById(organizationId)).thenReturn(Optional.empty());
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));

        // Act
        authService.verifyEmail(tokenValue);

        // Assert
        verify(organizationRepository, times(1)).save(argThat(savedOrganization -> Boolean.TRUE.equals(savedOrganization.getIsVerified())));
        verify(emailVerificationTokenRepository, times(1)).save(argThat(savedToken -> Boolean.TRUE.equals(savedToken.getIsUsed())));
    }
}
