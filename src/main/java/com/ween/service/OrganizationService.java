package com.ween.service;

import com.ween.dto.request.CreateOrganizationRequest;
import com.ween.dto.request.UpdateOrganizationRequest;
import com.ween.entity.Organization;
import com.ween.entity.User;
import com.ween.enums.SubscriptionPlan;
import com.ween.exception.ResourceNotFoundException;
import com.ween.mapper.OrganizationMapper;
import com.ween.repository.EventRepository;
import com.ween.repository.OrganizationRepository;
import com.ween.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final StorageService storageService;

    @Transactional
    public Organization createOrganization(CreateOrganizationRequest request, String ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + ownerId));

        Organization organization = Organization.builder()
                .name(request.getName())
                .description(request.getDescription())
                .contactEmail(request.getContactEmail())
                .ownerId(ownerId)
                .subscriptionPlan(SubscriptionPlan.FREE)
                .build();

        Organization saved = organizationRepository.save(organization);
        log.info("Organization created: {} by user: {}", saved.getName(), ownerId);
        return saved;
    }

    public Organization getOrganizationById(String organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
    }

    @Transactional
    public Organization updateOrganization(String organizationId, String id, UpdateOrganizationRequest request) {
        Organization organization = getOrganizationById(organizationId);

        if (request.getName() != null) {
            organization.setName(request.getName());
        }

        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }

        if (request.getContactEmail() != null) {
            organization.setContactEmail(request.getContactEmail());
        }

        Organization updated = organizationRepository.save(organization);
        log.info("Organization updated: {}", organizationId);
        return updated;
    }

    @Transactional
    public String uploadLogo(String organizationId, MultipartFile logoFile) {
        Organization organization = getOrganizationById(organizationId);

        // Delete old logo if exists
        if (organization.getLogoUrl() != null && !organization.getLogoUrl().isEmpty()) {
            try {
                storageService.deleteFile(organization.getLogoUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old logo", e);
            }
        }

        // Upload new logo
        String logoUrl = storageService.uploadOrganizationLogo(logoFile, organizationId);
        organization.setLogoUrl(logoUrl);
        organizationRepository.save(organization);

        log.info("Logo uploaded for organization: {}", organizationId);
        return logoUrl;
    }

    @Transactional
    public void deleteOrganization(String organizationId) {
        Organization organization = getOrganizationById(organizationId);
        organizationRepository.delete(organization);
        log.info("Organization deleted: {}", organizationId);
    }

    public Page<Organization> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable);
    }

    public long getOrganizationEventCount(String organizationId) {
        return eventRepository.countByOrganizationIdAndStatusIn(organizationId, java.util.List.of(
                com.ween.enums.EventStatus.DRAFT,
                com.ween.enums.EventStatus.PUBLISHED,
                com.ween.enums.EventStatus.ONGOING,
                com.ween.enums.EventStatus.COMPLETED
        ));
    }

    @Transactional
    public void upgradeSubscription(String organizationId, SubscriptionPlan plan) {
        Organization organization = getOrganizationById(organizationId);
        organization.setSubscriptionPlan(plan);
        organizationRepository.save(organization);
        log.info("Organization subscription upgraded to {}: {}", plan, organizationId);
    }

    public Integer getEventLimitForPlan(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> 5;
            case STARTER -> 20;
            case PROFESSIONAL -> 100;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }

    public long getRemainingEventSlots(String organizationId) {
        Organization organization = getOrganizationById(organizationId);
        Integer limit = getEventLimitForPlan(organization.getSubscriptionPlan());
        long currentCount = getOrganizationEventCount(organizationId);
        return Math.max(0, limit - currentCount);
    }

    public boolean canCreateEvent(String organizationId) {
        return getRemainingEventSlots(organizationId) > 0;
    }

    @Transactional
    public void transferOwnership(String organizationId, String newOwnerId) {
        Organization organization = getOrganizationById(organizationId);
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("New owner not found: " + newOwnerId));

        organization.setOwnerId(newOwnerId);
        organizationRepository.save(organization);
        log.info("Organization ownership transferred to {}: {}", newOwnerId, organizationId);
    }

    public Organization getOrganizationByOwner(String ownerId) {
        return organizationRepository.findAll().stream()
                .filter(org -> org.getOwnerId().equals(ownerId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found for owner: " + ownerId));
    }

    public Object getOrganizationAnalytics(String userId, String id) {
        return null;
    }
}
