-- V3__add_indexes.sql
-- Add additional indexes for performance

-- User indexes
ALTER TABLE users ADD INDEX idx_created_at (created_at DESC);
ALTER TABLE users ADD INDEX idx_is_email_verified (is_email_verified);

-- Organization indexes
ALTER TABLE organizations ADD INDEX idx_created_at (created_at DESC);

-- Event indexes
ALTER TABLE events ADD INDEX idx_organization_created (organization_id, created_at DESC);
ALTER TABLE events ADD INDEX idx_category_status (category, status);
ALTER TABLE events ADD INDEX idx_city_status (city, status);

-- Event Registration indexes
ALTER TABLE event_registrations ADD INDEX idx_user_event (user_id, event_id);
ALTER TABLE event_registrations ADD INDEX idx_joined_event (is_joined, event_id);

-- Coin Transaction indexes
ALTER TABLE coin_transactions ADD INDEX idx_user_reason (user_id, reason);
ALTER TABLE coin_transactions ADD INDEX idx_user_created (user_id, created_at DESC);

-- Notification indexes
ALTER TABLE notifications ADD INDEX idx_user_unread (user_id, is_read);
ALTER TABLE notifications ADD INDEX idx_user_created (user_id, created_at DESC);

-- Leaderboard indexes
ALTER TABLE leaderboard_entries ADD INDEX idx_period_scope_rank (period, scope, rank_position);

-- Certificate indexes
ALTER TABLE certificates ADD INDEX idx_user_created (user_id, created_at DESC);
ADD INDEX idx_user_event (user_id, event_id);
