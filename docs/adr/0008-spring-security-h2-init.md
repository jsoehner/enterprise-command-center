# 0008. Spring Security with BCrypt and In-Memory H2 Initialization

## Status
Accepted

## Context
During the initial development and testing phases, it is desirable to have a functional admin user available immediately without setting up a production-grade PostgreSQL database. We need a mechanism to securely store passwords and provide an easy way to seed the initial user.

## Decision
We will use Spring Security's `BCryptPasswordEncoder` to hash passwords. A `DatabaseInitializer` class will implement `CommandLineRunner` to check if an `admin` user exists in the database on startup. If not, it will create one using the `ADMIN_USERNAME` and `ADMIN_PASSWORD` provided via environment variables or the default configuration. 

For local development, we will use an in-memory H2 database.

## Consequences
- **Positive**: Zero-config start for new developers; the admin user is created automatically.
- **Positive**: BCrypt ensures that even the initial admin password is not stored in plain text in the database.
- **Negative**: H2 in-memory database is not a replacement for production PostgreSQL.
- **Trade-off**: While `CommandLineRunner` is easy for seeding, it should be used cautiously to avoid re-seeding in environments where data persistence is required.
