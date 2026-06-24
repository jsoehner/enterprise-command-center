# Project Memory

## 📖 Project Context
Apache Camel + Spring Boot 3.x project that aggregates multiple REST APIs into a unified backend-for-frontend interface with Redis caching.
**Tech Stack:**
- Java 17, Maven
- Spring Boot 3.2.4
- Apache Camel 4.6.0
- Redis (caching)

## 🎯 Current Objectives
- [x] Initialize `MEMORY.md` using `memory-skill.md` instructions.
- [ ] (Add future objectives here)

## 🧠 Key Decisions & Architecture
- **[2026-06-06] - Project Initialization:** Established the project memory structure and populated initial context from workspace information.
- **[2026-06-06] - Security Assessment:** Adopted a layered security pipeline approach based on the project's security assessment documents. Tools selected include SpotBugs and OWASP Dependency-Check for CI (Maven/Java), Trivy for Docker container scanning, Gitleaks for pre-commit secret detection, and OWASP ZAP for DAST integration testing.
- **[2026-06-06] - Security Scan Refinements:** Resolved SpotBugs `EI_EXPOSE_REP` warnings across DTOs and config classes by implementing defensive copies. Optimized OWASP Dependency-Check by adding a dedicated `actions/cache` step in GitHub Actions for the NVD database.
- **[2026-06-13] - Code Quality Improvements:** Addressed multiple linter warnings including unused imports, dead code, and type safety issues across various classes. Details captured in `Code-Cleanup-Lessons-Learned.md`.
- **[2026-06-11] - Security Scan Finding Policy (Option B):** Configured the security scan workflow to run non-blockingly (`continue-on-error: true`) and create an automated GitHub issue summarizing any scanner failures, rather than failing/blocking the build.
- **[2026-06-11] - Security Scan Auto-Trigger & Dependency Update Config:** Integrated the security scan workflow to trigger the dependency update workflow when security findings are detected. Removed Netty, Tomcat, Spring Security, and Kafka version exclusions from the nightly dependency update workflow to allow automated resolution of vulnerabilities.
- **[2026-06-21] - Security Scan Issue Notification Fix:** Fixed a bug in `parse-findings.js` where `findings-table.md` was unconditionally generated, causing scanner failures to mistakenly be reported as "No security findings detected in the parsing reports". Now, the workflow correctly falls back to listing the failed scan tools when no explicit findings are found.
## 👤 User Preferences
- Prefers CLI-only workflows for configuration activities; avoid UI-login-updated steps.
- Values retroactive documentation of completed activities and lessons learned in project docs.

## 📝 Unresolved Issues / Gotchas
- **OWASP NVD Rate Limits:** The initial Dependency-Check scan will download a massive vulnerability database which is rate-limited. Ensure the `NVD_API_KEY` is set in GitHub Repository Secrets to prevent build slowdowns or timeouts, as the workflow is configured to automatically use it.
