package com.ween.service;

import com.ween.dto.response.AdminStatsResponse;
import com.ween.dto.response.OrganizationResponse;
import com.ween.entity.Organization;
import com.ween.entity.User;
import com.ween.mapper.OrganizationMapper;
import com.ween.mapper.UserMapper;
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
public class AdminService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;

    public Page<com.ween.dto.response.UserResponse> getAllUsers(String search, Pageable pageable) {
        log.debug("Fetching all users with search: {}", search);
        
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(userMapper::toUserResponse);
    }

    public Page<OrganizationResponse> getAllOrganizations(String search, Pageable pageable) {
        log.debug("Fetching all organizations with search: {}", search);
        
        Page<Organization> organizations;
        if (search != null && !search.isBlank()) {
            organizations = organizationRepository.findByOrganizationNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search, search, pageable);
        } else {
            organizations = organizationRepository.findAll(pageable);
        }
        
        return organizations.map(organizationMapper::toOrganizationResponse);
    }

    public OrganizationResponse verifyOrganization(String organizationId, Boolean verify, String verificationNote) {
        log.debug("Verifying organization: {} with note: {}", organizationId, verificationNote);
        
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        organization.setVerified(true);
        organization.setVerificationNote(verificationNote);
        organizationRepository.save(organization);
        
        log.info("Organization verified successfully: {}", organizationId);
        return null;
    }

    public com.ween.dto.response.UserResponse banUnbanUser(String id, Boolean ban, String reason) {
        return null;
    }

    public AdminStatsResponse getPlatformStatistics() {
        return null;
    }
}
