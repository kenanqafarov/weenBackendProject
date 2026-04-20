/*
package com.ween.controller;


import com.ween.dto.request.CreateOrganizationRequest;
import com.ween.dto.request.UpdateOrganizationProfileRequest;
import com.ween.dto.request.UpdateOrganizationRequest;
import com.ween.dto.response.*;
import com.ween.entity.Organization;
import com.ween.security.SecurityUtil;
import com.ween.service.EventService;
import com.ween.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management endpoints")
public class OrganizationController {

    private final SecurityUtil securityUtil;
    private final OrganizationService organizationService;
    private final EventService eventService;

    @PostMapping
    @Transactional
    @Operation(summary = "Create organization", description = "Create a new organization (ORGANIZER only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Organization created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<Organization>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        try {
            String userId = securityUtil.getCurrentUserId();
            Organization response = organizationService.createOrganization(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(response, "Organization created successfully"));
        } catch (Exception e) {
            log.error("Failed to create organization for user: {}", getCurrentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization details", description = "Retrieve detailed information about an organization")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Organization retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<ApiResponse<Organization>> getOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable String id) {
        try {
            Organization response = organizationService.getOrganizationById(id);
            return ResponseEntity.ok(ApiResponse.ok(response, "Organization retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve organization: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update organization", description = "Update organization details (owner only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Organization updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only owner can update"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<ApiResponse<Organization>> updateOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        try {
            String userId = securityUtil.getCurrentUserId();
            Organization response = organizationService.updateOrganization(userId, id, request);
            return ResponseEntity.ok(ApiResponse.ok(response, "Organization updated successfully"));
        } catch (Exception e) {
            log.error("Failed to update organization: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/{id}/events")
    @Operation(summary = "Get organization events", description = "Retrieve pageable list of events organized by this organization")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getOrganizationEvents(
            @Parameter(description = "Organization ID", required = true) @PathVariable String id,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<EventResponse> response = eventService.getOrganizationEvents(id, pageable);
            return ResponseEntity.ok(ApiResponse.ok(response, "Events retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve events for organization: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/{id}/analytics")
    @Operation(summary = "Get organization analytics", description = "Get analytics dashboard for organization")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only owner can view analytics"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<ApiResponse<Object>> getOrganizationAnalytics(
            @Parameter(description = "Organization ID", required = true) @PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            Object response = organizationService.getOrganizationAnalytics(userId, id);
            return ResponseEntity.ok(ApiResponse.ok(response, "Analytics retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve analytics for organization: {}", id, e);
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
*/
