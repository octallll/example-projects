--liquibase formatted sql

--changeset sveshnikov:1
CREATE TABLE IF NOT EXISTS chats
(
    id         BIGINT PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

--changeset sveshnikov:2
CREATE TABLE IF NOT EXISTS links
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url              TEXT UNIQUE NOT NULL,
    last_update_time TIMESTAMP WITH TIME ZONE,
    last_check_at    TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

--changeset sveshnikov:3
CREATE TABLE IF NOT EXISTS subscriptions
(
    chat_id BIGINT REFERENCES chats (id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES links (id) ON DELETE CASCADE,
    tags    TEXT[],
    PRIMARY KEY (chat_id, link_id)
);

--changeset sveshnikov:4
CREATE INDEX IF NOT EXISTS idx_links_url ON links (url);
CREATE INDEX IF NOT EXISTS idx_subscriptions_chat_id ON subscriptions (chat_id);
