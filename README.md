# Spring Identity Service

This is a personal project I built to learn more about identity management,
authentication, OAuth, and permission-based authorization with Spring Boot.

It is a standalone REST API that manages users, credentials, sessions, social
accounts, roles, and permissions. It also publishes email and notification events
to RabbitMQ so they can be handled by a separate worker service.

## How It Works

The service uses PostgreSQL for persistent identity data:

- Users and password credentials
- Refresh tokens and active sessions
- Connected OAuth accounts
- Roles, permissions, and user role assignments

Redis stores temporary and high-frequency data:

- OAuth state
- Password reset and email verification state
- Rate-limit counters

RabbitMQ receives notification events for email verification, password reset,
OAuth welcome messages, and OAuth account-link notifications. This project only
publishes those events; it does not send emails itself.

Authentication is stateless. Short-lived JWT access tokens and rotating refresh
tokens are stored in HttpOnly cookies. Access tokens include the user's effective
permissions, which Spring Security enforces through method annotations.

## Built With

- Java 21
- Spring Boot 4
- Spring Security 7
- Spring Data JPA
- PostgreSQL
- Redis
- RabbitMQ
- Liquibase
- MapStruct and Lombok
- Docker and Docker Compose
- Maven, Spotless, and Checkstyle

## What It Can Do

### Authentication

- Register with email and password
- Verify an email address
- Resend email verification
- Login and logout
- Refresh and rotate authentication tokens
- Logout from every device
- Change a password
- Request and complete a password reset
- Protect state-changing requests with CSRF tokens

### Sessions

- List active sessions
- Identify the current session
- Revoke another session
- Prevent users from revoking their current session through the session endpoint

### OAuth

- Login with Google
- Register a new account through Google
- Link Google to an existing account
- List connected OAuth accounts
- Unlink an OAuth account
- Prevent unlinking the final available login method
- Add providers through a shared `OAuthProvider` abstraction

### Roles and Permissions

- Store permissions in PostgreSQL
- Create roles from one or more permissions
- Update and delete roles
- Enable or disable permissions without changing their keys
- Assign multiple roles to a user
- Include effective permissions in access tokens
- Protect controller operations with `@RequiresPermission`
- Protect the owner role and prevent removal of the final owner assignment

### Rate Limiting

- Configure limits per endpoint
- Apply limits by IP address or authenticated user
- Store counters and expiration windows in Redis
- Return `429 Too Many Requests` with a retry delay

### Notifications

- Publish email verification events
- Publish password reset events
- Publish OAuth welcome events
- Publish OAuth link events
- Use queue priorities for different notification types
- Route failed messages to a dead-letter queue

## Project Structure

```text
spring-identity/
|-- scripts/
|-- src/
|   |-- main/
|   |   |-- java/com/github/mohrezal/identity/
|   |   |   |-- config/
|   |   |   |-- domain/
|   |   |   |   |-- auth/
|   |   |   |   |-- privilege/
|   |   |   |   `-- user/
|   |   |   `-- shared/
|   |   `-- resources/
|   |       |-- db/changelog/
|   |       |-- application.yaml
|   |       `-- messages.properties
|   `-- test/
|-- docker-compose.yml
`-- pom.xml
```

The application layer follows a CQRS-style structure. Commands change state,
queries read state, and controllers handle HTTP concerns.

## How to Run

### Prerequisites

- Java 21 or newer
- Docker
- Docker Compose
- Git
- Google OAuth client credentials

### Steps

1. Clone the repository:

```bash
git clone https://github.com/mohrezal/spring-identity.git
cd spring-identity
```

2. Create a `.env` file in the project root:

```properties
APP_DATABASE_HOST=localhost
APP_DATABASE_PORT=5432
APP_DATABASE_NAME=identity
APP_DATABASE_USER=identity
APP_DATABASE_PASSWORD=identity

APP_REDIS_HOST=localhost
APP_REDIS_PORT=6379
APP_REDIS_PASSWORD=identity

APP_RABBITMQ_HOST=localhost
APP_RABBITMQ_PORT=5672
APP_RABBITMQ_CLIENT_PORT=15672
APP_RABBITMQ_USER=identity
APP_RABBITMQ_PASSWORD=identity

APP_OWNER_EMAIL=owner@example.com

APP_SECURITY_SECRET=replace-with-a-secret-containing-at-least-32-bytes
APP_SECURITY_ACCESS_TOKEN_TTL=15m
APP_SECURITY_REFRESH_TOKEN_TTL=14d
APP_SECURITY_VERIFICATION_TOKEN_TTL=30m
APP_SECURITY_PASSWORD_RESET_TOKEN_TTL=15m
APP_SECURITY_ALLOWED_ORIGIN=http://localhost:3000

APP_SECURITY_OAUTH_GOOGLE_CLIENT_ID=your-google-client-id
APP_SECURITY_OAUTH_GOOGLE_CLIENT_SECRET=your-google-client-secret
APP_SECURITY_OAUTH_GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/o/google/callback
```

You can generate a signing secret with:

```bash
openssl rand -base64 48
```

3. Start PostgreSQL, Redis, and RabbitMQ:

```bash
docker compose up -d
```

4. Start the application:

```bash
./mvnw spring-boot:run
```

Liquibase creates and validates the database schema during startup.

5. Seed the permission catalog and configured roles:

```bash
./scripts/seed-privilege.sh
```

6. Register the account configured by `APP_OWNER_EMAIL`, then assign its owner
   role:

```bash
./scripts/seed-owner.sh
```

The owner account must already exist before running the owner seeder.

## Accessing the Services

| Service | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI document | http://localhost:8080/v3/api-docs |
| RabbitMQ Management | http://localhost:15672 |

## Development

Run tests:

```bash
./mvnw test
```

Run formatting and static analysis:

```bash
./mvnw spotless:check checkstyle:check
```

Create a Liquibase migration:

```bash
./scripts/new-migration.sh add_example_table
```
