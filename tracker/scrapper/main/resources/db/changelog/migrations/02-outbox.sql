--liquibase formatted sql

--changeset sveshnikov:5
CREATE TABLE IF NOT EXISTS outbox
(
    id          UUID PRIMARY KEY,
    topic       TEXT NOT NULL,
    key_value   TEXT,
    payload     JSONB NOT NULL,
    status      TEXT DEFAULT 'PENDING',
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

--changeset sveshnikov:6
CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox (status) WHERE status = 'PENDING';
