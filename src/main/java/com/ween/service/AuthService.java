package com.ween.service;

import com.ween.dto.request.LoginRequest;
import com.ween.dto.request.ChangePasswordRequest;
import com.ween.dto.request.RegisterRequest;
import com.ween.dto.request.RegisterOrganizationRequest;
import com.ween.dto.request.ResetPasswordRequest;
import com.ween.dto.response.AuthResponse;
import com.ween.dto.response.UserResponse;
import com.ween.entity.EmailVerificationToken;
import com.ween.entity.PasswordResetToken;
import com.ween.entity.User;
import com.ween.entity.Organization;
import com.ween.enums.UserRole;
import com.ween.exception.AlreadyExistsException;
import com.ween.exception.InvalidTokenException;
import com.ween.exception.ResourceNotFoundException;
import com.ween.exception.UnauthorizedException;
import com.ween.repository.EmailVerificationTokenRepository;
import com.ween.repository.PasswordResetTokenRepository;
import com.ween.repository.UserRepository;
import com.ween.repository.OrganizationRepository;
import com.ween.security.JwtUtil;
import com.ween.security.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final CoinService coinService;
    private final com.ween.repository.ReferralRepository referralRepository;

    @Value("${ween.frontend.verify-url:https://ween.az/verify}")
    private String verifyEmailBaseUrl;

    @Value("${ween.frontend.reset-password-url:https://ween.az/change-password}")
    private String resetPasswordBaseUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Username already taken: " + request.getUsername());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .birthDate(request.getBirthDate())
                .phone(request.getPhone())
                .university(request.getUniversity())
                .major(request.getMajor())
                .course(request.getCourse())
                .interests(convertToJsonArray(request.getInterests()))
                .skills(convertToJsonArray(request.getSkills()))
                .role(UserRole.VOLUNTEER)
                .referralCode(generateReferralCode())
                .weenCoinBalance(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        try {
            createAndSendEmailVerification(savedUser);
        } catch (Exception e) {
            // Registration should still succeed even if email provider is temporarily unavailable.
            log.error("Failed to create/send verification email for user: {}", savedUser.getEmail(), e);
        }

        // Award signup bonus
        coinService.awardSignupBonus(savedUser.getId());

        // Handle referral if provided
        String referralCode = request.getReferralCode();
        if (referralCode != null && !referralCode.isEmpty()) {
            try {
                User referrer = userRepository.findByReferralCode(referralCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Invalid referral code"));
                
                // Create and award referral coins
                com.ween.entity.Referral referral = com.ween.entity.Referral.builder()
                        .referrerId(referrer.getId())
                        .referredId(savedUser.getId())
                        .coinAwarded(false)
                        .build();
                referralRepository.save(referral);
                
                // Award coins asynchronously
                coinService.awardReferralBonus(referrer.getId(), savedUser.getId());
                coinService.credit(savedUser.getId(), 100, com.ween.enums.CoinReason.REFERRAL, referrer.getId());
                log.info("Referral processed for new user: {}", savedUser.getId());
            } catch (Exception e) {
                log.warn("Failed to process referral code during registration", e);
            }
        }

        // Generate tokens for immediate login
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getEmail(),savedUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId());

        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
            .isEmailVerified(savedUser.getIsEmailVerified())
                .weenCoinBalance(savedUser.getWeenCoinBalance())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    @Transactional
    public AuthResponse registerOrganization(RegisterOrganizationRequest request) {
        // Extract email and username from request
        String orgEmail = request.getEmail();
        String orgUsername = request.getUsername();

        // Check if email already exists
        if (userRepository.existsByEmail(orgEmail)) {
            throw new AlreadyExistsException("Email already registered: " + orgEmail);
        }

        // Check if username already exists
        if (userRepository.existsByUsername(orgUsername)) {
            throw new AlreadyExistsException("Username already taken: " + orgUsername);
        }

        // Create organization admin user account
        User orgAdminUser = User.builder()
                .username(orgUsername)
                .email(orgEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getOrganizationName())
                .role(UserRole.ORGANIZATION_ADMIN)
                .referralCode(generateReferralCode())
                .build();

        User savedOrgAdminUser = userRepository.save(orgAdminUser);
        log.info("Organization admin user created: {}", savedOrgAdminUser.getEmail());

        // Create organization
        Organization organization = Organization.builder()
                .name(request.getOrganizationName())
                .description(request.getDescription())
                .ownerId(savedOrgAdminUser.getId())
                .isVerified(false)
                .build();

        organizationRepository.save(organization);
        log.info("Organization created: {}", organization.getName());

        // Generate tokens for immediate login
        String accessToken = jwtUtil.generateAccessToken(savedOrgAdminUser.getId(), savedOrgAdminUser.getEmail(),savedOrgAdminUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(savedOrgAdminUser.getId());

        UserResponse userResponse = UserResponse.builder()
                .id(savedOrgAdminUser.getId())
                .username(savedOrgAdminUser.getUsername())
                .email(savedOrgAdminUser.getEmail())
                .fullName(savedOrgAdminUser.getFullName())
                .role(savedOrgAdminUser.getRole())
            .isEmailVerified(savedOrgAdminUser.getIsEmailVerified())
                .weenCoinBalance(savedOrgAdminUser.getWeenCoinBalance())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(),user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("User logged in successfully: {}", user.getEmail());

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
            .isEmailVerified(user.getIsEmailVerified())
                .weenCoinBalance(user.getWeenCoinBalance())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    public String refreshToken(String refreshTokenStr) {
        try {
            String userId = jwtUtil.extractUserId(refreshTokenStr);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            return jwtUtil.generateAccessToken(user.getId(), user.getEmail(),user.getRole());
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        passwordResetTokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID() + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        PasswordResetToken token = PasswordResetToken.builder()
            .userId(user.getId())
            .token(rawToken)
            .expiresAt(expiresAt)
            .isUsed(false)
            .build();
        passwordResetTokenRepository.save(token);

        String resetLink = resetPasswordBaseUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
        log.info("Password reset email sent to: {}", email);
    }

    @Transactional
    public void resetPasswordWithToken(@Valid ResetPasswordRequest request) {
        String token = request.getToken();
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Reset token is required");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByTokenAndIsUsedFalse(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = userRepository.findById(passwordResetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate token immediately after successful password reset.
        passwordResetToken.setIsUsed(true);
        passwordResetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetToken);

        log.info("Password reset successfully via token for user: {}", user.getEmail());
    }

    @Transactional
    public void changePasswordForCurrentUser(@Valid ChangePasswordRequest request) {
        String oldPassword = request.getOldPassword();
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new UnauthorizedException("Old password is required");
        }

        String userId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    private String generateReferralCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String convertToJsonArray(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // Split by comma and create JSON array
            List<String> items = Arrays.asList(value.split(","));
            List<String> trimmedItems = new ArrayList<>();
            for (String item : items) {
                trimmedItems.add(item.trim());
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(trimmedItems);
        } catch (Exception e) {
            log.warn("Failed to convert string to JSON array: {}", value, e);
            return null;
        }
    }

    @Transactional
    public void logout() {
        try {
            String userId = securityUtil.getCurrentUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            log.info("User logged out successfully: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Logout process error for user", e);
            throw new UnauthorizedException("Logout failed");
        }
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
            .findByTokenAndIsUsedFalse(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        User user = userRepository.findById(verificationToken.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsEmailVerified(true);
        userRepository.save(user);

        verificationToken.setIsUsed(true);
        verificationToken.setVerifiedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    public void sendPasswordResetLink(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email) {
        initiatePasswordReset(email);
    }

    private void createAndSendEmailVerification(User user) {
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID() + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(user.getId())
                .token(rawToken)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        emailVerificationTokenRepository.save(token);

        String verificationLink = verifyEmailBaseUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);
    }
}
