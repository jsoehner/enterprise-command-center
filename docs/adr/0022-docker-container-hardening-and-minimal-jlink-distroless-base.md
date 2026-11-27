# ADR-0022: Docker Container Hardening and Minimal jlink Distroless Base

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Governance Team
* **Date:** 2026-11-27

---

## 1. Context and Problem Statement
The prior container build produced a **696 MB** image (`231 MB` compressed content size) on `cgr.dev/chainguard/jre:latest` (`~562 MB` base runtime + `134 MB` fat `app.jar`). Four security and operational flaws affected that build:

1. **Floating Base Tag & Runtime Drift**: `Dockerfile` pulled `cgr.dev/chainguard/jre:latest`, which drifted to OpenJDK `26.0.2.1` instead of the repository's pinned **Java 25 LTS** baseline (`pom.xml` `<java.version>25</java.version>`).
2. **Dockerfile Misconfigurations**: Trivy `config` scanning flagged three findings: `DS-0001` (MEDIUM: `:latest` base tag), `DS-0002` (HIGH: missing explicit `USER`), and `DS-0026` (LOW: missing `HEALTHCHECK`).
3. **Oversized Attack Surface & Fat-JAR Churn**: The build stage copied a single `134 MB` executable JAR, forced full layer invalidation via `RUN echo "Build triggered at $(date)"`, and left `.env`, `docs/`, `scripts/`, `tools/`, and `.mvn/` inside the Docker build context.
4. **Missing Runtime & Supply-Chain Controls**: `docker-run.sh` and `docker-run.ps1` bound ports to `0.0.0.0` without `--read-only`, `--cap-drop=ALL`, `no-new-privileges`, or memory/CPU/PID limits, and `docker-publish.yml` lacked SLSA provenance and SPDX SBOM attestations.

## 2. Decision Drivers & Quality Attributes
* **Security**: Eliminate root execution, shells, package managers, and floating tags; enforce read-only rootfs, dropped Linux capabilities, and loopback-by-default bindings.
* **Reliability / Availability**: Replace shell/curl probes with a zero-dependency Java health check (`ContainerHealthCheck`) and pin Java `25.0.5.1` LTS deterministically.
* **Performance / Scalability**: Shrink OS + JRE footprint with a stripped `jlink` runtime and extract Spring Boot 4 layers so code-only rebuilds push `692 kB` instead of `134 MB`.
* **Maintainability & Reversibility**: Keep standard Dockerfile stages and multi-arch (`linux/amd64`, `linux/arm64`) support via `$TARGETPLATFORM`.

## 3. Considered Options
| Option | Summary | Pros | Cons / Trade-offs |
| :--- | :--- | :--- | :--- |
| **Option 1** (Selected) | `cgr.dev/chainguard/glibc-dynamic` (`16.25 MB`, digest-pinned) + custom `jlink` Java 25.0.5.1 LTS JRE (`68.1 MB`) | Smallest glibc distroless footprint (`84.4 MB` OS+JRE), 7 Wolfi packages, 0 Trivy CVEs, UID `65532` non-root, no shell | Requires maintaining the explicit `jlink` module list when new JDK modules are needed |
| **Option 2** | `gcr.io/distroless/base-debian13:nonroot` (`37.41 MB`) + custom `jlink` Java 25 LTS JRE | 0 Trivy CVEs, Debian 13 glibc compatibility, UID `65532` | `21.16 MB` larger OS base than Chainguard `glibc-dynamic` |
| **Option 3** | `gcr.io/distroless/java25-debian13:nonroot` (`304.14 MB`) | Pre-built Temurin 25 LTS JRE without `jlink` step | Full unstripped JRE (`~220 MB` larger OS+JRE than Option 1) |
| **Option 4** | `bellsoft/liberica-runtime-container:jre-25-slim-musl` (`190.86 MB`) | Compact musl-based Alpine JRE | Ships shell/BusyBox (`ash`), defaults to `root` user, musl compatibility differences for JNI libraries |

## 4. Decision Outcome
Chosen option: **Option 1 (`cgr.dev/chainguard/glibc-dynamic` + custom `jlink` Java 25 LTS JRE)**, because it cuts OS + JRE size by **85.0%** (`~562 MB -> 84.4 MB`), pins Java `25.0.5.1` LTS, preserves glibc compatibility, and passes Trivy config and vulnerability gates with zero findings.

### Implemented Hardening Controls
1. **3-Stage Digest-Pinned `Dockerfile`**:
   - **Stage 1 (`build`, `$BUILDPLATFORM`)**: Uses digest-pinned `maven:3.9-eclipse-temurin-25`, removes the non-deterministic `date` layer, extracts Spring Boot 4 layers via `java -Djarmode=tools -jar ... extract --layers --launcher`, and removes write permissions (`chmod -R a-w`).
   - **Stage 2 (`jre-builder`, `$TARGETPLATFORM`)**: Uses digest-pinned `eclipse-temurin:25-jdk-noble` to build a stripped `jlink` JRE (`--strip-debug --no-man-pages --no-header-files --compress=zip-6`) with modules `java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.jfr,jdk.management,jdk.naming.dns,jdk.unsupported,jdk.zipfs` and sets `chmod -R a-w`.
   - **Stage 3 (`runtime`, `$TARGETPLATFORM`)**: Uses digest-pinned `cgr.dev/chainguard/glibc-dynamic@sha256:a96ba3c8d5ffc646e1092c03d72000b5e2ccd9f6cda695d6342a0e31ce4d52ee` (`7` Wolfi packages), copies the JRE and 4 Spring Boot layers owned by `65532:65532`, sets `USER 65532:65532`, adds OCI metadata labels, configures `STOPSIGNAL SIGTERM`, runs `ContainerHealthCheck` via `HEALTHCHECK`, and sets `-Dvertx.cacheDirBase=/tmp/vertx-cache` for read-only rootfs compatibility.
2. **Distroless Health Check (`ContainerHealthCheck.java`)**: Probes `http://127.0.0.1:${SERVER_PORT:-8080}/` using only `java.base` (`HttpURLConnection`) so `HEALTHCHECK` runs without `curl`, `wget`, or `/bin/sh`.
3. **Build Context Isolation (`.dockerignore`)**: Excludes `.env`, `.env.*`, `.mvn/`, `target/`, `docs/`, `oss/`, `scripts/`, `tools/`, `security_agent/`, and `config/`, shrinking build context transfer to `8.97 kB`.
4. **Hardened Launch Scripts (`docker-run.sh`, `docker-run.ps1`)**: Enforce `ADMIN_PASSWORD`, bind to `127.0.0.1` by default (`BIND_HOST`), and run with `--read-only`, `--tmpfs /tmp:rw,noexec,nosuid,size=64m`, `--cap-drop=ALL`, `--security-opt=no-new-privileges:true`, `--user 65532:65532`, `--memory=1g`, `--cpus=2`, `--pids-limit=256`, and `--restart=unless-stopped`.
5. **CI/CD Supply-Chain Gates**: Added `provenance: mode=max` and `sbom: true` in `.github/workflows/docker-publish.yml`, and added a blocking Trivy `config` scan (`CRITICAL,HIGH,MEDIUM`, `exit-code: 1`) in `.github/workflows/security-testing.yml`.

## 5. Consequences & Trade-offs
### Positive Consequences
* **44.3% Smaller Uncompressed Image**: Total disk usage dropped from **696 MB** (`231 MB` content) to **388 MB** (`172 MB` content, `-25.5%`); OS + JRE layers dropped from `~562 MB` to `84.4 MB` (`-85.0%`).
* **Fast Incremental Layers**: Application-code layer shrank from `134 MB` to `692 kB`.
* **Zero Security Findings**: Trivy `config` scan (`LOW,MEDIUM,HIGH,CRITICAL`) reported `0` misconfigurations; Trivy `image` scan (`HIGH,CRITICAL`, `--ignore-unfixed`) reported `0` vulnerabilities.
* **Verified Runtime Hardening**: `mvn test` passed `25/25`; live hardened container (`--read-only`, `--cap-drop=ALL`, `no-new-privileges`) passed `ContainerHealthCheck` (`Health=healthy`), confirmed `/bin/sh` absent, and served `/`, `/camel/orders/summary`, `/camel/pqc/status`, `/camel/pqc/demo-exchange`, and `/actuator/health` (`UP`).

### Negative Consequences & Operational Costs
* **Digest & Module Maintenance**: Base image SHA-256 digests and the `jlink` `--add-modules` list require explicit updates when upgrading JDK versions or adding dependencies that need unlisted JDK modules.

### Follow-on Actions & Migration Plan
- [x] Implement `ContainerHealthCheck.java`, `ContainerHealthCheckTest.java`, hardened `Dockerfile`, and `.dockerignore`.
- [x] Harden `docker-run.sh`, `docker-run.ps1`, `.github/workflows/docker-publish.yml`, and `.github/workflows/security-testing.yml`.
- [x] Verify `mvn test` (`25/25`), Trivy `config` (`0`), Trivy `image` (`0`), and live `--read-only` container probes.
- [x] Update `README.md`, `docs/DEVELOPER_GUIDE.md`, and `docs/security/SECURITY_POSTURE_EVALUATION.md`.
