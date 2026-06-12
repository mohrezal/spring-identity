--liquibase formatted sql
--changeset mohammadreza:1781110722_add_user_oauth_connections splitStatements:false
CREATE TABLE user_oauth_connections (
id UUID NOT NULL DEFAULT uuid_generate_v4(),
user_id UUID NOT NULL,
provider VARCHAR(50) NOT NULL,
provider_user_id VARCHAR(255) NOT NULL,
email VARCHAR(255),
created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT pk_user_oauth_connections PRIMARY KEY (id),
CONSTRAINT uq_user_oauth_connections_provider UNIQUE (provider, provider_user_id),
CONSTRAINT fk_user_oauth_connections_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS user_oauth_connections;