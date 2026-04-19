-- V6__add_organization_admin_role.sql
-- Add ORGANIZATION_ADMIN role to the existing role enum

ALTER TABLE users MODIFY role ENUM('VOLUNTEER','ORGANIZER','ORGANIZATION_ADMIN','ADMIN') DEFAULT 'VOLUNTEER';

