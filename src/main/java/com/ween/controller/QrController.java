package com.ween.controller;

import com.ween.dto.request.CheckinRequest;
import com.ween.dto.response.ApiResponse;
import com.ween.dto.response.CheckinResponse;
import com.ween.dto.response.QrResponse;
import com.ween.service.QrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
@Tag(name = "QR Code", description = "QR code and check-in endpoints")
public class QrController {

    private final QrService qrService;

    @GetMapping("/my-qr")
    @Operation(summary = "Get my QR code", description = "Get authenticated user's QR code for check-in")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "QR code retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<QrResponse>> getMyQrCode() {
        try {
            String userId = getCurrentUserId();
            QrResponse response = qrService.generateQrCode(userId);
            return ResponseEntity.ok(ApiResponse.ok(response, "QR code retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to generate QR code for user: {}", getCurrentUserId(), e);
            throw e;
        }
    }

    @PostMapping("/checkin")
    @Transactional
    @Operation(summary = "Check-in to event", description = "Check-in participant to an event using QR token (API Key authentication)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Check-in successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid QR token or invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing API key"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event or participant not found")
    })
    public ResponseEntity<ApiResponse<CheckinResponse>> checkin(
            @Valid @RequestBody CheckinRequest request) {
        try {
            CheckinResponse response = qrService.checkinParticipant(request.getEventId(), request.getQrToken());
            return ResponseEntity.ok(ApiResponse.ok(response, "Check-in successful"));
        } catch (Exception e) {
            log.error("Failed to check-in participant for event: {}", request.getEventId(), e);
            throw e;
        }
    }

    @GetMapping("/events/{id}/live")
    @Operation(summary = "Get live event statistics", description = "Get real-time event check-in statistics")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Live stats retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<ApiResponse<Object>> getLiveEventStats(
            @Parameter(description = "Event ID", required = true) @PathVariable String id) {
        try {
            Object response = qrService.getLiveEventStats(id);
            return ResponseEntity.ok(ApiResponse.ok(response, "Live statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve live stats for event: {}", id, e);
            throw e;
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return (String) authentication.getPrincipal();
    }
}
