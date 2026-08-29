# ADR 0005: Java 25 Runtime Migration and CI Container Publishing Resilience

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & DevOps Team
* **Date:** 2026-08-29

---

## 1. Context & Business Problem Statement

To maintain maximum performance, forward compatibility, and modern JVM capabilities (such as Virtual Threads, enhanced JIT optimizations, and tightened memory safety), `enterprise-command-center` is transitioning its runtime and build toolchains from Java 21 to **Java 25**.

Simultaneously, pull request workflows (such as Dependabot automated dependency bumps) encountered build failures in the Docker publishing pipeline (`docker-publish.yml`) due to:
1. Attempting unconditional Docker Hub authentication during PR builds when secrets (`DOCKER_USERNAME` / `DOCKER_TOKEN`) are not provided to Dependabot/fork contexts.
2. Referencing the `secrets` context directly within step-level `if:` expressions in GitHub Actions workflows, which violates expression syntax rules.
3. Attempting to pull non-existent Alpine variants of Eclipse Temurin Maven images for Java 25.

---

## 2. Decision Drivers

1. **Java 25 Language & Runtime Baseline**: Leverage Java 25 compiler target and runtime optimizations across Spring Boot 3.5, Apache Camel 4.22, Tomcat 11, and Netty 4.2.
2. **Static Analysis & Bytecode Compatibility**: Ensure SAST tools (SpotBugs 4.10.4.0) and annotation processors (Lombok, MapStruct) support Java 25 bytecode (class format version 69).
3. **CI/CD Reliability & Least Privilege**: Eliminate PR build failures caused by missing third-party registry secrets during pull request validations.
4. **Valid Multi-Stage Container Packaging**: Use official, supported Docker build images for Java 25 (`maven:3.9-eclipse-temurin-25`).

---

## 3. Decision Outcome

Chosen Strategy: **Full Project Upgrade to Java 25, Step-Level Secret Decoupling via Job Environment Variables, and Official Java 25 Build Container Adoption**.

### Key Architectural Changes

1. **Build Configuration (`pom.xml`)**:
   - Set `<java.version>25</java.version>`.
   - Upgraded `spotbugs-maven-plugin` to `4.10.4.0` for Java 25 bytecode compatibility.

2. **Container Build (`Dockerfile`)**:
   - Updated multi-stage build image from `maven:3.9.6-eclipse-temurin-21-alpine` to `maven:3.9-eclipse-temurin-25`.
   - Maintained Chainguard minimal JRE base for lightweight, CVE-minimized runtime security.

3. **GitHub Actions Workflow Modernization**:
   - Updated `actions/setup-java` across all workflows (`auto-bump-version.yml`, `nightly-dependency-update.yml`, `pr-validation.yml`, `security-scan.yml`, `semantic-release.yml`, `update-nvd-cache.yml`) to `java-version: '25'`.
   - Mapped `DOCKER_USERNAME` and `DOCKER_TOKEN` secrets into job-level `env` blocks in `docker-publish.yml`, allowing safe conditional evaluation in step-level `if: github.event_name != 'pull_request' && env.DOCKER_USERNAME != ''` checks.

---

## 4. Consequences & Trade-Offs

### Positive
* **Next-Gen JVM Performance**: Enables modern Project Loom virtual thread scaling in Apache Camel routes and Spring Web endpoints.
* **Resilient CI/CD**: Dependabot and community PRs build and test smoothly without failing on missing external registry credentials.
* **Deterministic Toolchains**: Java 25 is standardized across developer environments, Docker containers, and CI pipelines.

### Trade-Offs
* **Image Size**: The `maven:3.9-eclipse-temurin-25` build container uses Ubuntu/Debian base instead of Alpine, but runtime container size remains minimal via Chainguard JRE.

---

## 5. Implementation Checklist

- [x] Update `<java.version>` to 25 and `spotbugs-maven-plugin` to 4.10.4.0 in `pom.xml`
- [x] Update `Dockerfile` build stage to `maven:3.9-eclipse-temurin-25`
- [x] Map secrets to job `env` in `.github/workflows/docker-publish.yml`
- [x] Upgrade `actions/setup-java` to `java-version: '25'` across 6 workflow files
- [x] Update ADR documentation and index
