# Build & Functional Verification Report

- **Date**: 2026-11-20
- **Verified By**: Jeffs' Automated build and deploy agent
- **Repository**: `jsoehner/enterprise-command-center` (`rest-api-aggregator` `0.9.89-SNAPSHOT`, Spring Boot `4.2.0-RC2`, Apache Camel `4.23.0`)
- **Host Environment**: Windows 11 (`amd64`), JDK `25.0.1` (Oracle), Apache Maven `3.10.0`

## Trusted OSS Supply Chain Summary

- **Registry**: `http://oss.jsigroup.local:8080/maven/virtual` (Healthcheck `/healthz`: `200`)
- **Git Hygiene**: `.env`, `uv.toml`, `.mvn/settings.xml`, `.mvn/maven.config`, and `.npmrc` excluded via `.git/info/exclude`.
- **Vulnerability Gate Remediations Applied (HTTP 451 -> Clean Versions)**:
  - `org.codehaus.plexus:plexus-utils:3.6.0` / `3.5.1` (`GHSA-6fmv-xxpf-w3cw`, CVSS 7.5) -> upgraded to `3.6.1` in `maven-resources-plugin:3.5.0`, `maven-compiler-plugin:3.16.0`, and `spring-boot-maven-plugin:4.2.0-M2`.
  - `commons-io:commons-io:2.11.0` -> upgraded to `2.22.0` via `maven-shared-utils:3.5.0` exclusion in `maven-compiler-plugin:3.16.0` and `maven-dependency-plugin:3.11.0`.
  - `tools.jackson.core:jackson-databind:3.1.6` -> upgraded to `3.3.0` in `spring-boot-maven-plugin:4.2.0-RC2`.
  - `org.xerial.snappy:snappy-java:1.1.10.7` -> pinned to `1.1.10.12` in `<dependencyManagement>`.

## Build & Run Commands

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
$env:PATH = "$env:JAVA_HOME\bin;C:\Tools\apache-maven-3.10.0\bin;$env:PATH"

# Unit & integration tests + executable JAR repackage
mvn package -s .mvn/settings.xml "-Dmaven.artifact.threads=1" "-Daether.connector.basic.threads=1"

# Launch on isolated verification port
java -jar target/rest-api-aggregator-0.9.89-SNAPSHOT.jar --server.port=18080 --websocket.port=0 --bucket4j.enabled=false
```

## Functional Verification Evidence

### 1. Unit & Integration Tests (`mvn test`)
- **Result**: **22/22 passed** (`0` failures, `0` errors, `0` skipped)
  - `AggregatorRouteTest`: 4/4 passed
  - `DatabaseInitializerTest`: 3/3 passed
  - `PostQuantumCryptoServiceTest`: 3/3 passed
  - `WorkOrderServiceTest`: 12/12 passed

### 2. API Functional Probes (`api_verify.py`)
| Endpoint | Auth | HTTP Status | Result |
| :--- | :--- | :---: | :--- |
| `GET /` | Public | `200` | PASS (HTML command center shell) |
| `GET /dashboard-view.html` | Public | `200` | PASS (HTML dashboard view) |
| `GET /camel/orders/summary` | None | `401` | PASS (unauthenticated rejected) |
| `GET /actuator/health` | Basic (`admin`) | `200` | PASS (`{"groups":["liveness","readiness"],"status":"UP"}`) |
| `GET /camel/orders/summary` | Basic (`admin`) | `200` | PASS (JSON queue & revenue summary) |
| `GET /camel/pqc/status` | Basic (`admin`) | `200` | PASS (`NIST FIPS 203 (ML-KEM)`, `ML-KEM-768`, `ACTIVE`) |
| `GET /camel/pqc/demo-exchange` | Basic (`admin`) | `200` | PASS (`keyExchangeStatus: "SUCCESS"`) |
| `GET /camel/health/status` | Basic (`admin`) | `200` | PASS (all 5 downstream services `UP`) |

### 3. Guided Headless Chrome CDP Verification (`browser_verify.py`)
- `http://localhost:18080/`: `ok: true`, HTTP `200`, title `"Enterprise Command Center - REST API Aggregator"`, `expect_text_found: true`, `console_errors: []`, `exceptions: []`
- `http://localhost:18080/design-system.html`: `ok: true`, HTTP `200`, title `"Enterprise Design System - Style Guide & Component Showcase"`, `console_errors: []`, `exceptions: []`
