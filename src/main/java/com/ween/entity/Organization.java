package com.ween.entity;

import lombok.*;
import jakarta.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "organizations", indexes = {
    @Index(name = "idx_owner_id", columnList = "owner_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(length = 200, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(length = 300)
    private String website;

    @Column(name = "owner_id", columnDefinition = "CHAR(36)", nullable = false)
    private String ownerId;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verification_note", columnDefinition = "TEXT")
    private String verificationNote;

    public void setVerified(boolean b) {
    }
}
