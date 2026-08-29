# 0007. Docker Deployment with Environment Variable Injection

## Status
Accepted

## Context
The application needs to be easily deployable in a containerized environment (Docker). However, hardcoding sensitive credentials like the administrator password in the `application.yml` file or the Docker image itself poses a security risk and makes it difficult to manage different environments (dev, staging, prod).

## Decision
We will use `docker-run.sh` and `docker-run.ps1` scripts to manage container lifecycle. These scripts will explicitly inject the `ADMIN_USERNAME` and `ADMIN_PASSWORD` as environment variables into the Docker container at runtime.

The Spring Boot application is configured to read these from `${ADMIN_USERNAME}` and `${ADMIN_PASSWORD}` (defaulting to `admin` and `admin123` respectively), allowing for easy overrides without changing the code or image.

## Consequences
- **Positive**: Improved security by preventing hardcoded secrets in the image.
- **Positive**: Simplified local development setup as developers can use the scripts directly.
- **Negative**: Developers must ensure they have the correct environment variables set or passed through the script.
- **New Risk**: If environment variables are logged or leaked in shell history, they could be exposed (mitigated by not hardcoding them in the scripts but allowing local overrides).
