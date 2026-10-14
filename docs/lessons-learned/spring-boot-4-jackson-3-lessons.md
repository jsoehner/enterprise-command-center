# Lessons Learned: Spring Boot 4, Jackson 3, and Container Vulnerability Remediation

## 1. Context
During the migration of `enterprise-command-center` to Spring Boot `4.2.0-M2` and Spring Security `7.2.0-M2`, multiple breaking changes, deprecations, and container security scan failures were identified and remediated.

---

## 2. Key Learnings & Resolutions

### 1. Test Harness Modernization (`TestRestTemplate` Removal)
- **Issue**: In Spring Boot 4, `TestRestTemplate` was removed from `spring-boot-starter-test`. Integration tests attempting to autowire `TestRestTemplate` failed compilation.
- **Remediation**: Transitioned test harnesses (such as `AggregatorRouteTest.java`) to standard Java 25 `java.net.http.HttpClient` with `@LocalServerPort`. Basic Authentication headers were encoded directly using `Base64.getEncoder().encodeToString()`.
- **Lesson**: Relying on standard Java HTTP clients rather than framework-specific wrappers improves long-term test resilience across major framework version jumps.

### 2. Spring Data Redis Deprecations
- **Issue**: `GenericJackson2JsonRedisSerializer` in Spring Data Redis was marked deprecated for removal in favor of Jackson 3 serialization.
- **Remediation**: Replaced explicit instantiation with `RedisSerializer.json()`, allowing Spring Data Redis to automatically resolve and utilize the active Jackson 3 serializer.

### 3. Dual Jackson Classpath & Trivy Container Scan Failures
- **Issue**: Even after upgrading Spring Boot to `4.2.0-M2`, container vulnerability scans (`aquasecurity/trivy-action` executing Trivy on the built container image `local-build:${{ github.sha }}`) failed with exit code 1.
- **Root Cause**:
  1. Spring Boot `4.2.0-M2` defaulted to Jackson BOM `3.1.6`, which preceded upstream DoS patches.
  2. `pom.xml` continued using legacy `camel-jackson-starter:4.22.1` (Jackson 2) instead of `camel-jackson3-starter`.
  3. `pom.xml` had pinned `com.fasterxml.jackson.core:2.22.3` in `dependencyManagement`, causing dual Jackson 2 and Jackson 3 implementations to be packaged into `app.jar`.
- **Remediation**:
  1. Overrode `<jackson-bom.version>3.2.3</jackson-bom.version>` to upgrade Jackson 3 to patched release `3.2.3`.
  2. Migrated Apache Camel starter to `camel-jackson3-starter:4.22.1`.
  3. Overrode `<jackson-2-bom.version>2.22.3</jackson-2-bom.version>` to patch transitive Jackson 2 dependencies required by OpenAPI/Swagger.
- **Lesson**: When upgrading to major milestone framework releases, always verify transitive starters (e.g. Camel starters) and override milestone BOM properties when upstream patches are already available on Maven Central.

### 4. Git Push Concurrency in Continuous Integration
- **Issue**: Workflows that commit and push reports back to the repository (`auto-bump-version.yml` and `sbom-cbom.yml`) occasionally failed due to concurrent push rejections (`[rejected] main -> main (fetch first)`).
- **Remediation**: Implemented a 5-attempt retry loop with `git pull --rebase origin main` before each push.
- **Lesson**: Any automated CI workflow that pushes to the default branch must employ exponential backoff and rebase loops to handle concurrent pipeline completions gracefully.

### 5. Security Audit & Remediation Highlights
During the `piolium` security review, several critical vulnerabilities were identified and remediated in this update cycle:

- **PQC Timing Side-Channel (C1)**: Upgraded Bouncy Castle to `1.86` to mitigate cryptographic timing attacks in the ML-KEM implementation.
- **PostgreSQL JDBC Bypass (H1)**: Upgraded JDBC driver to `42.7.14` to ensure proper `requireAuth` enforcement for database connections.
- **WorkOrder Concurrency (M1)**: Refactored `WorkOrderService` to use `ConcurrentHashMap.computeIfPresent` for atomic state transitions, preventing order processing race conditions.
- **Order ID Injection (L1)**: Implemented strict regex validation (`^ORD-[A-Z0-9]{8}$`) for all Order ID inputs to prevent injection attacks.
- **Actuator Exposure (L2)**: Restricted `/actuator/health` and `/actuator/info` endpoints to `ROLE_ADMIN` to prevent unauthorized information disclosure.

---

## 3. Conclusion
Regularly addressing code quality warnings—such as dead code, unused imports, and type safety issues—keeps the codebase healthy, readable, and maintainable. Suppressing warnings should only be done thoughtfully (e.g., with `@SuppressWarnings("unchecked")` or explicitly marking `@NonNull` parameters) when we can guarantee the safety of the operation.
