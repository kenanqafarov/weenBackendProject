package com.ween.service;

import com.ween.dto.request.LoginRequest;
import com.ween.dto.request.RegisterRequest;
import com.ween.dto.request.ResetPasswordRequest;
import com.ween.entity.User;
import com.ween.enums.UserRole;
import com.ween.exception.AlreadyExistsException;
import com.ween.exception.ResourceNotFoundException;
import com.ween.exception.UnauthorizedException;
import com.ween.repository.UserRepository;
import com.ween.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    // private final EmailService emailService; // DISABLED
    private final CoinService coinService;

    @Transactional
    public User register(RegisterRequest request) {
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
                .role(UserRole.VOLUNTEER)
                .referralCode(generateReferralCode())
                .weenCoinBalance(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

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
                
                com.ween.repository.ReferralRepository referralRepo = null; // Will be injected in production
                // Award coins asynchronously
                coinService.awardReferralBonus(referrer.getId(), savedUser.getId());
                coinService.credit(savedUser.getId(), 100, com.ween.enums.CoinReason.REFERRAL, referrer.getId());
                log.info("Referral processed for new user: {}", savedUser.getId());
            } catch (Exception e) {
                log.warn("Failed to process referral code during registration", e);
            }
        }

        // Send welcome email asynchronously (DISABLED)
        // try {
        //     emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        // } catch (Exception e) {
        //     log.warn("Failed to send welcome email", e);
        // }

        return savedUser;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("User logged in successfully: {}", user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        return response;
    }

    public String refreshToken(String refreshTokenStr) {
        try {
            String userId = jwtUtil.extractUserId(refreshTokenStr);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            return jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String resetToken = jwtUtil.generateRefreshToken(user.getId());
        
        // In production, store this token in Redis with expiration
        // For now, send the link via email
        String resetLink = "https://app.ween.com/reset-password?token=" + resetToken;
        
        // Send password reset email (DISABLED)
        // try {
        //     emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
        //     log.info("Password reset email sent to: {}", email);
        // } catch (Exception e) {
        //     log.error("Failed to send password reset email", e);
        //     throw new RuntimeException("Failed to send password reset email");
        // }
        log.info("Password reset requested for: {}", email);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        try {
            String userId = jwtUtil.extractUserId(resetToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password reset successfully for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to reset password", e);
            throw new UnauthorizedException("Invalid or expired reset token");
        }
    }

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    private String generateReferralCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void logout() {
    }

    public void verifyEmail(String token) {
    }

    public void sendPasswordResetLink(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email) {
    }

    public void resetPassword(@Valid ResetPasswordRequest request) {
    }
}
