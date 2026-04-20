-- V7__add_organization_password_hash.sql
-- Add passwordHash field to organizations table

ALTER TABLE organizations 
ADD COLUMN password_hash VARCHAR(255) AFTER name;
