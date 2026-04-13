package com.ween.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {
    private String id;
    private String name;
    private String description;
    private String logoUrl;
    private String contactEmail;
    private String website;
    private String ownerId;
    private Boolean isVerified;
    private String verificationNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
