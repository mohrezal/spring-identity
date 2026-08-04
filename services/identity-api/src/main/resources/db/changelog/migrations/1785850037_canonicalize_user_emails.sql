--liquibase formatted sql
--changeset mohammadreza:1785850037_canonicalize_user_emails splitStatements:false
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM users
        GROUP BY LOWER(BTRIM(email))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Cannot canonicalize user emails because multiple users share a canonical email';
    END IF;
END
$$;

UPDATE users
SET email = LOWER(BTRIM(email))
WHERE email <> LOWER(BTRIM(email));

UPDATE user_oauth_connections
SET email = LOWER(BTRIM(email))
WHERE email IS NOT NULL
  AND email <> LOWER(BTRIM(email));

ALTER TABLE users
    ADD CONSTRAINT ck_users_email_canonical
    CHECK (email = LOWER(BTRIM(email))) NOT VALID;

ALTER TABLE users
    VALIDATE CONSTRAINT ck_users_email_canonical;

ALTER TABLE user_oauth_connections
    ADD CONSTRAINT ck_user_oauth_connections_email_canonical
    CHECK (email IS NULL OR email = LOWER(BTRIM(email))) NOT VALID;

ALTER TABLE user_oauth_connections
    VALIDATE CONSTRAINT ck_user_oauth_connections_email_canonical;

--rollback ALTER TABLE user_oauth_connections DROP CONSTRAINT IF EXISTS ck_user_oauth_connections_email_canonical;
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_email_canonical;
