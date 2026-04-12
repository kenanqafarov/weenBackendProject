-- Add ban fields to users table
ALTER TABLE users ADD COLUMN is_banned TINYINT(1) DEFAULT 0;
ALTER TABLE users ADD COLUMN ban_reason TEXT NULL;

-- Add verification_note field to organizations table
ALTER TABLE organizations ADD COLUMN verification_note TEXT NULL;

-- Create index for banned users query
CREATE INDEX idx_is_banned ON users(is_banned);
