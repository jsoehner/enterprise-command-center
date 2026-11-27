# Developer Guide

## 🚀 Getting Started

This project uses Docker, Java 25 LTS, Apache Camel 4.23.0, Spring Boot 4.2.0-RC2, and Bouncy Castle 1.87 (Post-Quantum Cryptography) for a hardened development and runtime environment.

### Prerequisites
- **Docker** installed and running.
- **Java 25** (Temurin / OpenJDK).
- **Apache Maven 3.9+**.
- **Python 3.10+** (for ADR Gatekeeper governance tooling).

---

## 🛠 Running the Application

### 1. Local Maven Execution
```bash
# Run unit and integration tests
mvn clean test

# Run application locally with H2 in-memory DB and dev profiles
mvn spring-boot:run
```

### 2. Using the Hardened Docker Scripts
Launch scripts for Bash and PowerShell enforce environment-provided credentials and container hardening flags:
- **Linux / macOS / Git Bash**: `ADMIN_PASSWORD='your-strong-password' ./docker-run.sh`
- **Windows (PowerShell)**: `$env:ADMIN_PASSWORD='your-strong-password'; .\docker-run.ps1`

Both scripts:
1. Reject missing or default (`admin123`) `ADMIN_PASSWORD` values before pulling or starting the container.
2. Pull the multi-arch image (defaults to `jsoehner/enterprise-command-center:main`; override with `TAG`).
3. Remove any previous container instance.
4. Bind ports `8080` and `8081` to `127.0.0.1` (override with `BIND_HOST`) and apply `--read-only`, `--tmpfs /tmp:rw,noexec,nosuid,size=64m`, `--cap-drop=ALL`, `--security-opt=no-new-privileges:true`, `--user 65532:65532`, `--memory=1g`, `--cpus=2`, `--pids-limit=256`, and `--restart=unless-stopped`.

### 3. Local Container Build & Trivy Verification
```bash
# Build the 3-stage distroless image locally
docker build -t enterprise-command-center:local .

# Scan Dockerfile for misconfigurations (0 expected across all severities)
docker run --rm -v "$PWD:/work:ro" -w /work aquasec/trivy:latest config --severity LOW,MEDIUM,HIGH,CRITICAL --exit-code 1 Dockerfile

# Scan image for OS and Java library vulnerabilities
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 enterprise-command-center:local
```

### Configuration Overrides
Override default settings with environment variables:
- `ADMIN_USERNAME`: Administrative account username (defaults to `admin`).
- `ADMIN_PASSWORD`: Required administrative account password (`admin123` is rejected by `docker-run` scripts and by the `prod` Spring profile).
- `BIND_HOST`: Host interface for Docker port forwarding (defaults to `127.0.0.1`).
- `TAG`: Container image tag (defaults to `main`).
- `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL target properties when running with the `prod` profile.

---

## 🛡 Architectural Decision Records (ADR) Governance

This repository enforces architecture governance using **ADR Gatekeeper**:
- **Pre-commit Verification**: Automatic scanning of staged files before commits.
- **Integrity Validation**: Ensures immutable ADR sequences, valid templates, and bidirectional status transitions.

```bash
# Verify integrity of all ADRs
python3 scripts/adr_gatekeeper.py --verify

# Scan currently staged changes for Architectural Significance Requirements (ASR)
python3 scripts/adr_gatekeeper.py --scan-staged

# Regenerate tools/adr_index.json and docs/adr/README.md
python3 scripts/adr_gatekeeper.py --reindex
```

---

## 🧹 Repository Hygiene & Git Practices

1. **Clean Workspace Policy**:
   - Never commit build output (`target/`), temporary logs (`*.log`), scan output (`findings-table.md`), or agent transcripts (`piolium/`, `*.jsonl`).
   - All standard ignores are strictly enforced via [`.gitignore`](../.gitignore).
2. **Branching Strategy**:
   - Create feature branches (`feat/`, `fix/`, `chore/`) off `main`.
   - Open Pull Requests to trigger automated CI, SAST/SCA security scans, and ADR Gatekeeper validation.
   - Merged branches must be deleted post-merge to maintain a clean repository.

---

## 📚 Knowledge Base & Lessons Learned

Technical insights and operational lessons learned are archived under [`docs/lessons-learned/`](lessons-learned/):
- **[Code Cleanup Lessons Learned](lessons-learned/code-cleanup-lessons.md)** — Strategies for linting, dependency pruning, and codebase health.
- **[Nightly Dependencies Lessons Learned](lessons-learned/nightly-dependencies-lessons.md)** — Continuous dependency updates, Maven versioning, and CI permission models.
- **[Spring Boot 4 & Jackson 3 Lessons Learned](lessons-learned/spring-boot-4-jackson-3-lessons.md)** — Framework modernization, Jackson 3 alignment, and container vulnerability remediation.
- **[Architectural Decision Index](adr/README.md)** — Complete catalog of ADRs 0001 through 0023.
- **[Frequently Asked Questions](FAQ.md)** — Common questions regarding authentication, rate limiting, and API aggregator routes.
