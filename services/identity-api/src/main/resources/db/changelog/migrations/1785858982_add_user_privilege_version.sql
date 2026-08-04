--liquibase formatted sql
--changeset mohammadreza:1785858982_add_user_privilege_version splitStatements:false
ALTER TABLE users
    ADD COLUMN privilege_version BIGINT NOT NULL DEFAULT 0;
