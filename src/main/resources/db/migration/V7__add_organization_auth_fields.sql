-- V7__add_organization_auth_fields.sql
-- Add username, email, password_hash and category fields to organizations table

ALTER TABLE organizations 
ADD COLUMN username VARCHAR(50) UNIQUE AFTER id,
ADD COLUMN email VARCHAR(150) UNIQUE AFTER username,
ADD COLUMN password_hash VARCHAR(255) NOT NULL AFTER email,
ADD COLUMN category VARCHAR(50) AFTER website;

-- Add indexes for the new columns
CREATE INDEX idx_org_username ON organizations(username);
CREATE INDEX idx_org_email ON organizations(email);
