package com.ween.service;

import com.ween.dto.request.UpdateProfileRequest;
import com.ween.dto.response.PublicProfileResponse;
import com.ween.dto.response.UserResponse;
import com.ween.entity.User;
import com.ween.exception.ResourceNotFoundException;
import com.ween.mapper.UserMapper;
import com.ween.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final StorageService storageService;
    private final CoinService coinService;

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional
    public User updateProfile(String userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getUniversity() != null) {
            user.setUniversity(request.getUniversity());
        }

        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        User updated = userRepository.save(user);
        log.info("User profile updated: {}", userId);

        // Award profile complete bonus if all fields are filled
        if (isProfileComplete(updated)) {
            coinService.awardProfileCompleteBonus(userId);
        }

        return updated;
    }

    @Transactional
    public String uploadProfilePhoto(String userId, MultipartFile photoFile) {
        User user = getUserById(userId);

        // Delete old photo if exists
        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
            try {
                storageService.deleteFile(user.getProfilePhotoUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old profile photo", e);
            }
        }

        // Upload new photo
        String photoUrl = storageService.uploadProfilePhoto(photoFile, userId);
        user.setProfilePhotoUrl(photoUrl);
        userRepository.save(user);

        log.info("Profile photo uploaded for user: {}", userId);
        return photoUrl;
    }

    public boolean isProfileComplete(User user) {
        return user.getFullName() != null && !user.getFullName().isEmpty()
                && user.getBirthDate() != null
                && user.getPhone() != null && !user.getPhone().isEmpty()
                && user.getUniversity() != null && !user.getUniversity().isEmpty()
                && user.getMajor() != null && !user.getMajor().isEmpty()
                && user.getBio() != null && !user.getBio().isEmpty()
                && user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty();
    }

    public Integer getUserCoinBalance(String userId) {
        User user = getUserById(userId);
        return user.getWeenCoinBalance();
    }

    public User getUserWithStats(String userId) {
        User user = getUserById(userId);
        // Additional stats can be added here in future
        return user;
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("User deleted: {}", userId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User updateUserRole(String userId, com.ween.enums.UserRole role) {
        User user = getUserById(userId);
        user.setRole(role);
        User updated = userRepository.save(user);
        log.info("User role updated to {} for user: {}", role, userId);
        return updated;
    }

    public String getUserReferralCode(String userId) {
        User user = getUserById(userId);
        return user.getReferralCode();
    }


    public PublicProfileResponse getPublicProfile(String username) {
        return null;
    }

    public UserResponse updateProfilePhoto(String userId, String photoUrl) {
        return null;
    }
}
