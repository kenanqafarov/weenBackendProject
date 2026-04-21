package com.ween.mapper;

import com.ween.dto.response.OrganizationResponse;
import com.ween.entity.Organization;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-21T18:21:12+0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class OrganizationMapperImpl implements OrganizationMapper {

    @Override
    public OrganizationResponse toOrganizationResponse(Organization organization) {
        if ( organization == null ) {
            return null;
        }

        OrganizationResponse.OrganizationResponseBuilder organizationResponse = OrganizationResponse.builder();

        organizationResponse.id( organization.getId() );
        organizationResponse.username( organization.getUsername() );
        organizationResponse.organizationName( organization.getOrganizationName() );
        organizationResponse.description( organization.getDescription() );
        organizationResponse.logoUrl( organization.getLogoUrl() );
        organizationResponse.email( organization.getEmail() );
        organizationResponse.website( organization.getWebsite() );
        organizationResponse.role( organization.getRole() );
        organizationResponse.isVerified( organization.getIsVerified() );
        organizationResponse.verificationNote( organization.getVerificationNote() );
        organizationResponse.createdAt( organization.getCreatedAt() );
        organizationResponse.updatedAt( organization.getUpdatedAt() );

        return organizationResponse.build();
    }
}
