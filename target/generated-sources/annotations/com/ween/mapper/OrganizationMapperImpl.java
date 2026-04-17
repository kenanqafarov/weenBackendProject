package com.ween.mapper;

import com.ween.dto.response.OrganizationResponse;
import com.ween.entity.Organization;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-17T14:02:35+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class OrganizationMapperImpl implements OrganizationMapper {

    @Override
    public OrganizationResponse toOrganizationResponse(Organization organization) {
        if ( organization == null ) {
            return null;
        }

        OrganizationResponse.OrganizationResponseBuilder organizationResponse = OrganizationResponse.builder();

        organizationResponse.contactEmail( organization.getContactEmail() );
        organizationResponse.createdAt( organization.getCreatedAt() );
        organizationResponse.description( organization.getDescription() );
        organizationResponse.id( organization.getId() );
        organizationResponse.isVerified( organization.getIsVerified() );
        organizationResponse.logoUrl( organization.getLogoUrl() );
        organizationResponse.name( organization.getName() );
        organizationResponse.ownerId( organization.getOwnerId() );
        organizationResponse.updatedAt( organization.getUpdatedAt() );
        organizationResponse.verificationNote( organization.getVerificationNote() );
        organizationResponse.website( organization.getWebsite() );

        return organizationResponse.build();
    }
}
