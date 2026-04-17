package com.ween.controller;

import com.ween.dto.request.*;
import com.ween.dto.response.ApiResponse;
import com.ween.dto.response.AuthResponse;
import com.ween.entity.User;
import com.ween.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with optional referral code")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @Parameter(description = "Optional referral code")
            @RequestParam(value = "ref", required = false) String referralCode) {
        try {
            if (referralCode != null && !referralCode.isEmpty()) {
                request.setReferralCode(referralCode);
            }
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(response, "User registered successfully"));
        } catch (Exception e) {
            log.error("Registration failed", e);
            throw e;
        }
    }

    @PostMapping("/register/organization")
    @Operation(summary = "Register new organization", description = "Create a new organization account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Organization registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Organization already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> registerOrganization(
            @Valid @RequestBody RegisterOrganizationRequest request) {
        try {
            AuthResponse response = authService.registerOrganization(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(response, "Organization registered successfully"));
        } catch (Exception e) {
            log.error("Organization registration failed", e);
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive access token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Use refresh token to get a new access token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<ApiResponse<String>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.ok(response, "Token refreshed successfully"));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw e;
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate the current access token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Void>> logout() {
        try {
            authService.logout();
            return ResponseEntity.ok(ApiResponse.ok(null, "Logout successful"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw e;
        }
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verify user email using verification token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam(value = "token") String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.ok(null, "Email verified successfully"));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            throw e;
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset link")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset link sent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.sendPasswordResetLink(request.getEmail());
            return ResponseEntity.ok(ApiResponse.ok(null, "Password reset link sent to your email"));
        } catch (Exception e) {
            log.error("Forgot password request failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(ApiResponse.ok(null, "Password reset successfully"));
        } catch (Exception e) {
            log.error("Password reset failed", e);
            throw e;
        }
    }
}
