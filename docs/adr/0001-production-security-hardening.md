# ADR: Transition to Production-Hardened Infrastructure and Security Configuration

* Status: accepted
* Deciders: [Project Team]
* Date: 2024-05-22

## Technical Story
[Link to security-audit-results]

## Context and Problem Statement
The application was initially configured for rapid development, which introduced several high-risk security and operational flaws:
1. **Credential Exposure**: Hardcoded administrative credentials in `application.yml` posed a significant risk of exposure via version control.
2. **Data Volatility**: The use of an in-memory H2 database meant all production data would be lost upon application restart.
3. **Security Headers**: The application lacked standard browser-level security headers (CSP, HSTS, X-Content-Type-Options), leaving users vulnerable to clickjacking and XSS.

## Decision Drivers
* **Security**: Protecting sensitive credentials and user data.
* **Reliability**: Ensuring data persistence and availability.
* **Compliance**: Meeting industry standards for secure configuration and data handling.
* **Scalability**: Moving toward a production-ready database (PostgreSQL).

## Considered Options

### Option 1: Maintain current "Development-First" configuration
Keep hardcoded secrets, H2 database, and default Spring Security headers.

### Option 2: Implement Production-Hardened Configuration
Transition to environment-variable-based secrets, a dedicated PostgreSQL production profile, and explicit security headers.

## Decision Outcome
Chosen option: **Option 2**, because it establishes a production-ready security baseline that protects credentials, ensures data persistence, and implements defense-in-depth at the browser level.

### Positive Consequences
* **Secret Security**: Credentials are no longer stored in the codebase.
* **Data Persistence**: PostgreSQL ensures data survives restarts and allows for complex querying.
* **Defense-in-Depth**: CSP and HSTS provide an extra layer of protection against common web attacks.
* **Environment Parity**: Clear separation between `application.yml` (common) and `application-prod.yml` (production-specific).

### Negative Consequences
* **Deployment Complexity**: Requires configuring environment variables and a PostgreSQL instance in the production environment.
* **Configuration Overhead**: Team members must ensure they have the correct production profiles active during deployment.

## Pros and Cons of Options

### Option 1: Maintain current configuration
* **Good, because**: It is the simplest path to deployment and requires zero infrastructure changes.
* **Bad, because**: It is fundamentally insecure and unsuitable for any production use case.

### Option 2: Implement Production-Hardened Configuration
* **Good, because**: It aligns with industry best practices, protects user data, and provides a scalable foundation for growth.
* **Bad, because**: It adds a layer of operational complexity to the deployment pipeline.

---
**Next Steps:**
- [x] Create ADR
- [x] Remove Hardcoded Secrets
- [x] Implement Production Profile
- [x] Implement Security Headers
- [x] Harden Management Endpoints
- [ ] Verify that `ADMIN_USERNAME` and `ADMIN_PASSWORD` are set in the production environment
- [ ] Confirm PostgreSQL connectivity in the production environment
- [ ] Audit the CSP policy periodically to ensure it allows necessary frontend functionality
