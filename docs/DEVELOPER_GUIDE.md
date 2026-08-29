# Developer Guide

## Getting Started

This project uses Docker for consistent development environments.

### Prerequisites
- Docker installed and running.
- Java 25 (or compatible version).
- Maven.

### Running the Application

We provide scripts for both Bash and PowerShell to quickly spin up the application with the correct configuration.

#### Using the Docker Scripts
Run the script corresponding to your OS:
- **Linux/macOS/Git Bash**: `./docker-run.sh`
- **Windows (PowerShell)**: `.\docker-run.ps1`

These scripts will:
1. Pull the latest image.
2. Clean up any existing containers.
3. Start a new container with the default `admin/admin123` credentials.

#### Default Credentials
- **Username**: `admin`
- **Password**: `admin123`

### Configuration Overrides
You can override the default credentials by setting environment variables before running the scripts:
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`

### Architecture & Design
- **Database**: Uses H2 in-memory for local development.
- **Security**: Uses Spring Security with BCrypt.
- **Messaging/Integration**: Powered by Apache Camel.

Refer to `docs/adr/` for detailed Architectural Decision Records.
