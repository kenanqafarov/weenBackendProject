package com.ween.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {
    private String name;
    private String description;
    private String logoUrl;
    
    @Email(message = "Contact email must be valid")
    private String contactEmail;
    
    private String website;
}
