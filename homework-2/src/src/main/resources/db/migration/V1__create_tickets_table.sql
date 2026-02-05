-- V1__create_tickets_table.sql
-- Initial schema for ticket support system

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create tickets table
CREATE TABLE tickets (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Customer Information
    customer_id VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,

    -- Ticket Content
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,

    -- Classification
    category VARCHAR(50) DEFAULT 'OTHER',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(30) DEFAULT 'NEW',

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE,

    -- Assignment
    assigned_to VARCHAR(100),

    -- Tags (stored as JSON array)
    tags JSONB DEFAULT '[]'::jsonb,

    -- Metadata (flattened)
    metadata_source VARCHAR(20),
    metadata_browser VARCHAR(100),
    metadata_device_type VARCHAR(20),

    -- Constraints
    CONSTRAINT chk_category CHECK (category IN ('ACCOUNT_ACCESS', 'TECHNICAL_ISSUE', 'BILLING_QUESTION', 'FEATURE_REQUEST', 'BUG_REPORT', 'OTHER')),
    CONSTRAINT chk_priority CHECK (priority IN ('URGENT', 'HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT chk_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'WAITING_CUSTOMER', 'RESOLVED', 'CLOSED')),
    CONSTRAINT chk_metadata_source CHECK (metadata_source IS NULL OR metadata_source IN ('WEB_FORM', 'EMAIL', 'API', 'CHAT', 'PHONE')),
    CONSTRAINT chk_metadata_device_type CHECK (metadata_device_type IS NULL OR metadata_device_type IN ('DESKTOP', 'MOBILE', 'TABLET'))
);

-- Indexes for common queries
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_category ON tickets(category);
CREATE INDEX idx_tickets_priority ON tickets(priority);
CREATE INDEX idx_tickets_customer_id ON tickets(customer_id);
CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);
CREATE INDEX idx_tickets_status_category ON tickets(status, category);
CREATE INDEX idx_tickets_status_priority ON tickets(status, priority);

-- Comments
COMMENT ON TABLE tickets IS 'Customer support tickets';
COMMENT ON COLUMN tickets.id IS 'Unique ticket identifier (UUID)';
COMMENT ON COLUMN tickets.customer_id IS 'Customer identifier';
COMMENT ON COLUMN tickets.customer_email IS 'Customer email address';
COMMENT ON COLUMN tickets.customer_name IS 'Customer display name';
COMMENT ON COLUMN tickets.subject IS 'Ticket subject line (1-200 chars)';
COMMENT ON COLUMN tickets.description IS 'Ticket description (10-2000 chars)';
COMMENT ON COLUMN tickets.category IS 'Ticket category for routing';
COMMENT ON COLUMN tickets.priority IS 'Ticket priority level';
COMMENT ON COLUMN tickets.status IS 'Current ticket status';
COMMENT ON COLUMN tickets.created_at IS 'Ticket creation timestamp';
COMMENT ON COLUMN tickets.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN tickets.resolved_at IS 'Resolution timestamp (when status becomes RESOLVED)';
COMMENT ON COLUMN tickets.assigned_to IS 'Assigned agent identifier';
COMMENT ON COLUMN tickets.tags IS 'JSON array of tag strings';
COMMENT ON COLUMN tickets.metadata_source IS 'Ticket source channel';
COMMENT ON COLUMN tickets.metadata_browser IS 'Customer browser info';
COMMENT ON COLUMN tickets.metadata_device_type IS 'Customer device type';
