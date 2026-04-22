package com.ween.service;

import com.ween.dto.request.CreateOrganizationRequest;
import com.ween.dto.request.UpdateOrganizationRequest;
import com.ween.entity.Organization;
import com.ween.entity.User;
import com.ween.exception.AlreadyExistsException;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Organization createOrganization(CreateOrganizationRequest request, String ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ownerId));

        if (organizationRepository.existsByEmail(owner.getEmail())) {
            throw new AlreadyExistsException("Organization already exists for email: " + owner.getEmail());
        }

        if (organizationRepository.existsByUsername(owner.getUsername())) {
            throw new AlreadyExistsException("Organization already exists for username: " + owner.getUsername());
        }

        Organization organization = Organization.builder()
                .username(owner.getUsername())
                .email(owner.getEmail())
                .passwordHash(owner.getPasswordHash())
                .organizationName(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .website(request.getWebsite())
                .build();

        Organization saved = organizationRepository.save(organization);
        log.info("Organization created: {} by user: {}", saved.getOrganizationName(), ownerId);
        return saved;
    }

    public Organization getOrganizationById(String organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
    }

    @Transactional
    public Organization updateOrganization(String userId,String organizationId, UpdateOrganizationRequest request) {
        Organization organization = getOrganizationById(organizationId);

        if (!organization.getId().equals(userId)) {
            throw new RuntimeException("Only organization owner can update");
        }


        if (request.getName() != null) {
            organization.setOrganizationName(request.getName());
        }

        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }

        if (request.getContactEmail() != null) {
            organization.setEmail(request.getContactEmail());
        }

        if (request.getLogoUrl() != null) {
            organization.setLogoUrl(request.getLogoUrl());
        }

        if (request.getWebsite() != null) {
            organization.setWebsite(request.getWebsite());
        }

        Organization updated = organizationRepository.save(organization);
        log.info("Organization updated: {}", organizationId);
        return updated;
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

    public long getRemainingEventSlots(String organizationId) {
        return Long.MAX_VALUE;
    }

    public boolean canCreateEvent(String organizationId) {
        return getRemainingEventSlots(organizationId) > 0;
    }

    @Transactional
    public void transferOwnership(String organizationId, String newOwnerId) {
        throw new UnsupportedOperationException("Ownership transfer is not supported for organization accounts");
    }

    public Organization getOrganizationByOwner(String ownerId) {
        return organizationRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found for owner: " + ownerId));
    }

    public Object getOrganizationAnalytics(String userId, String id) {
        return null;
    }
}
