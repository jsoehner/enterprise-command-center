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
- **Issue**: Even after upgrading Spring Boot to `4.2.0-M2`, container vulnerability scans (`aquasecurity/trivy-action`) failed on `app.jar` with four High-severity CVEs:
  - `CVE-2026-89407`: Regular expression backtracking DoS in `tools.jackson.core:jackson-core:3.1.6`.
  - `CVE-2026-89425`: Unbounded StringBuilder growth DoS in `tools.jackson.core:jackson-core:3.1.6`.
  - `CVE-2026-91776`: Cache growth DoS in `tools.jackson.core:jackson-databind:3.1.6`.
  - `CVE-2026-91777`: Quadratic forward-reference completion DoS in `tools.jackson.core:jackson-databind:3.1.6`.
- **Root Cause**:
  1. Spring Boot `4.2.0-M2` defaulted to Jackson BOM `3.1.6`, which preceded upstream DoS patches.
  2. `pom.xml` continued using legacy `camel-jackson-starter:4.22.1` (Jackson 2 / `com.fasterxml.jackson.core`) instead of `camel-jackson3-starter:4.22.1`.
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
