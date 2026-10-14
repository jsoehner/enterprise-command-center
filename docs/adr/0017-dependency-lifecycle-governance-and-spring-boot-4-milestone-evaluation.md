# ADR 0017: Dependency Lifecycle Governance, Safe Baseline Hardening, and Spring Boot 4 Milestone Evaluation

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Operations Governance Team
* **Date:** 2026-10-14

---

## 1. Context & Business Problem Statement

The Enterprise Command Center (`rest-api-aggregator`) is an enterprise-grade Backend-for-Frontend (BFF) connecting microservices, Kafka event streams, Redis cache/rate-limiting, and hybrid Post-Quantum Cryptographic (PQC) endpoints. Maintaining dependency health, software supply chain security, and continuous compatibility with upstream frameworks requires systematic governance across 295 resolved dependencies.

During scheduled dependency lifecycle orchestration (`dependency-lifecycle-orchestrator` and `dependency-updater`), the following landscape was analyzed:
1. **Supply Chain & Vulnerability Posture**: Zero SAST findings via SpotBugs and internal pattern scanning; valid SPDX SBOM and CycloneDX v1.7 CBOM (`oss/cbom.json`) tracking 5 crypto assets with 50.0% PQC migration readiness (NIST FIPS 203 ML-KEM-768 and FIPS 204 ML-DSA-65).
2. **Safe Patch/Minor Upgrades Available**: Incremental updates to `httpcore5.version` (`5.5-beta3` $\rightarrow$ `5.5-beta4`), `spotbugs-maven-plugin` (`4.10.4.1` $\rightarrow$ `4.10.5.0`), `spotbugs` (`4.10.4` $\rightarrow$ `4.10.5`), and ASM (`9.10.1` $\rightarrow$ `9.11`).
3. **Major Milestone Releases Upstream**: Upstream Spring ecosystem milestones were released: `spring-boot-starter-parent` `4.2.0-M2`, `spring-security` `7.2.0-M2`, and `assertj-core` `4.0.0-M1`. Major pre-releases cannot be deployed directly to production `main` without isolation, validation of breaking changes, and regression verification.

---

## 2. Decision Drivers

1. **Production Stability & SemVer Governance**: Never apply major breaking version bumps to the `main` production branch without an isolated rollout and complete test verification.
2. **Safe Automated Patching**: Immediately apply and verify backward-compatible minor/patch dependency and security tool updates on `main`.
3. **Exploratory Migration Validation**: Establish a dedicated testing branch (`test/deferred-major-upgrades`) to validate Spring Boot 4.x milestone compatibility, discover breaking architectural shifts early, and document required refactoring.
4. **Test Harness Modernization**: Resolve breaking removals in the Spring Boot 4 testing suite (specifically the deprecation and removal of `TestRestTemplate` from `spring-boot-starter-test`).
5. **Architectural Traceability**: Capture all findings in Architectural Decision Records, update documentation indexes, and sync with local memory systems.

---

## 3. Decision Outcome

1. **Production Baseline Hardening on `main`**:
   - Upgraded `httpcore5.version` from `5.5-beta3` to `5.5-beta4` in `pom.xml`.
   - Upgraded `spotbugs-maven-plugin` to `4.10.5.0`, `spotbugs` to `4.10.5`, and `asm` to `9.11`.
   - Verified that all 10 unit and integration tests (`mvn test`), SpotBugs SAST checks, and BOM tests (`scripts/test_boms.sh`) pass cleanly with zero regressions.
   - Preserved critical LTS pins: `camel.version` (4.22.1), `bouncycastle.version` (1.86), and `bucket4j-spring-boot-starter` (0.14.0).

2. **Isolated Major Upgrade Branch (`test/deferred-major-upgrades`)**:
   - Created dedicated branch to test `spring-boot-starter-parent` `4.2.0-M2`, `spring-security` `7.2.0-M2`, and `assertj-core` `4.0.0-M1`.
   - **Breaking Change Remediation**: Migrated `AggregatorRouteTest.java` from the removed `TestRestTemplate` to Java 25 standard `java.net.http.HttpClient` with `@LocalServerPort` and HTTP Basic Auth headers.
   - **Deprecation Auditing**: Documented the upcoming removal of `GenericJackson2JsonRedisSerializer` in Spring Data Redis and `AbstractAssert.asList()` in AssertJ 4.x.
   - **Verification**: Verified clean build (`mvn test`), with 10/10 test cases passing and SpotBugs/CBOM validations intact on the milestone branch.

3. **CI/CD Harmonization**:
   - Preserved existing nightly update exclusion rules in `.github/workflows/nightly-dependency-update.yml` to prevent unintended auto-merging of pre-release milestones on `main`.

---

## 4. Consequences & Verification

- **Positive**: `main` remains on a fully tested, zero-vulnerability Spring Boot 3.5.16 LTS baseline with up-to-date tooling and dependencies.
- **Positive**: The engineering team has proven compatibility and documented breaking migration paths for Spring Boot 4.x on `test/deferred-major-upgrades`, reducing future technical debt when Spring Boot 4 reaches General Availability.
- **Verification**:
  - `python3 scripts/adr_gatekeeper.py --verify` passes all integrity checks.
  - `mvn test` executes with 10 passing tests across both branches.
  - `scripts/test_boms.sh` confirms valid SBOM and CBOM artifacts with 50% PQC migration completion.
