package com.ween.mapper;

import com.ween.dto.response.PublicProfileResponse;
import com.ween.dto.response.UserResponse;
import com.ween.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-19T04:10:46+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.email( user.getEmail() );
        userResponse.fullName( user.getFullName() );
        userResponse.birthDate( user.getBirthDate() );
        userResponse.phone( user.getPhone() );
        userResponse.university( user.getUniversity() );
        userResponse.major( user.getMajor() );
        userResponse.bio( user.getBio() );
        userResponse.profilePhotoUrl( user.getProfilePhotoUrl() );
        userResponse.weenCoinBalance( user.getWeenCoinBalance() );
        userResponse.role( user.getRole() );
        userResponse.isEmailVerified( user.getIsEmailVerified() );
        userResponse.linkedinUrl( user.getLinkedinUrl() );
        userResponse.githubUrl( user.getGithubUrl() );
        userResponse.interests( user.getInterests() );
        userResponse.skills( user.getSkills() );
        userResponse.referralCode( user.getReferralCode() );

        return userResponse.build();
    }

    @Override
    public PublicProfileResponse toPublicProfileResponse(User user) {
        if ( user == null ) {
            return null;
        }

        PublicProfileResponse.PublicProfileResponseBuilder publicProfileResponse = PublicProfileResponse.builder();

        publicProfileResponse.id( user.getId() );
        publicProfileResponse.username( user.getUsername() );
        publicProfileResponse.fullName( user.getFullName() );
        publicProfileResponse.birthDate( user.getBirthDate() );
        publicProfileResponse.university( user.getUniversity() );
        publicProfileResponse.major( user.getMajor() );
        publicProfileResponse.bio( user.getBio() );
        publicProfileResponse.profilePhotoUrl( user.getProfilePhotoUrl() );
        publicProfileResponse.weenCoinBalance( user.getWeenCoinBalance() );
        publicProfileResponse.linkedinUrl( user.getLinkedinUrl() );
        publicProfileResponse.githubUrl( user.getGithubUrl() );
        publicProfileResponse.interests( user.getInterests() );
        publicProfileResponse.skills( user.getSkills() );

        return publicProfileResponse.build();
    }
}
