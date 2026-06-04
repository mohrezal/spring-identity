--liquibase formatted sql
--changeset mohammadreza:1780560899_init_user_management_schema splitStatements:false
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email_verified_at TIMESTAMPTZ,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY(id),
    CONSTRAINT uq_users_email UNIQUE(email)
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE user_credentials(
    user_id UUID NOT NULL,
    hashed_password VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_credentials PRIMARY KEY(user_id),
    CONSTRAINT fk_user_credentials_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_credentials_user_id ON user_credentials(user_id);

CREATE TABLE refresh_tokens(
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    hashed_token TEXT NOT NULL,
    device_info VARCHAR(300),
    ip_address TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hashed_token UNIQUE (hashed_token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

--rollback DROP TABLE IF EXISTS refresh_tokens;
--rollback DROP TABLE IF EXISTS user_credentials;
--rollback DROP TABLE IF EXISTS users;
