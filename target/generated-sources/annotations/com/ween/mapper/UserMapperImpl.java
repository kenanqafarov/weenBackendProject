package com.ween.mapper;

import com.ween.dto.response.PublicProfileResponse;
import com.ween.dto.response.UserResponse;
import com.ween.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T17:19:15+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.bio( user.getBio() );
        userResponse.birthDate( user.getBirthDate() );
        userResponse.email( user.getEmail() );
        userResponse.fullName( user.getFullName() );
        userResponse.githubUrl( user.getGithubUrl() );
        userResponse.id( user.getId() );
        userResponse.interests( user.getInterests() );
        userResponse.isEmailVerified( user.getIsEmailVerified() );
        userResponse.linkedinUrl( user.getLinkedinUrl() );
        userResponse.major( user.getMajor() );
        userResponse.phone( user.getPhone() );
        userResponse.profilePhotoUrl( user.getProfilePhotoUrl() );
        userResponse.referralCode( user.getReferralCode() );
        userResponse.role( user.getRole() );
        userResponse.skills( user.getSkills() );
        userResponse.university( user.getUniversity() );
        userResponse.username( user.getUsername() );
        userResponse.weenCoinBalance( user.getWeenCoinBalance() );

        return userResponse.build();
    }

    @Override
    public PublicProfileResponse toPublicProfileResponse(User user) {
        if ( user == null ) {
            return null;
        }

        PublicProfileResponse.PublicProfileResponseBuilder publicProfileResponse = PublicProfileResponse.builder();

        publicProfileResponse.bio( user.getBio() );
        publicProfileResponse.birthDate( user.getBirthDate() );
        publicProfileResponse.fullName( user.getFullName() );
        publicProfileResponse.githubUrl( user.getGithubUrl() );
        publicProfileResponse.id( user.getId() );
        publicProfileResponse.interests( user.getInterests() );
        publicProfileResponse.linkedinUrl( user.getLinkedinUrl() );
        publicProfileResponse.major( user.getMajor() );
        publicProfileResponse.profilePhotoUrl( user.getProfilePhotoUrl() );
        publicProfileResponse.skills( user.getSkills() );
        publicProfileResponse.university( user.getUniversity() );
        publicProfileResponse.username( user.getUsername() );
        publicProfileResponse.weenCoinBalance( user.getWeenCoinBalance() );

        return publicProfileResponse.build();
    }
}
