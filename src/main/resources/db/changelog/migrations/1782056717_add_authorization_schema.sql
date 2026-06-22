--liquibase formatted sql
--changeset mohammadreza:1782056717_add_authorization_schema splitStatements:false
CREATE TABLE roles (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    key VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_key UNIQUE (key)
);

CREATE INDEX idx_roles_key ON roles(key);
CREATE INDEX idx_roles_enabled ON roles(enabled);

CREATE TABLE role_permissions (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    role_id UUID NOT NULL,
    permission_key VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_role_permissions PRIMARY KEY (id),
    CONSTRAINT uq_role_permissions_role_permission UNIQUE (role_id, permission_key),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_role_permissions_permission_key ON role_permissions(permission_key);

CREATE TABLE user_roles (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_roles PRIMARY KEY (id),
    CONSTRAINT uq_user_roles_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

--rollback DROP TABLE IF EXISTS user_roles;
--rollback DROP TABLE IF EXISTS role_permissions;
--rollback DROP TABLE IF EXISTS roles;
