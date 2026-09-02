# Developer Guide

## 🚀 Getting Started

This project uses Docker, Java 25, Apache Camel 4.22, Spring Boot 3.5, and Bouncy Castle 1.85 (Post-Quantum Cryptography) for a modern, production-hardened development environment.

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

### 2. Using the Docker Scripts
We provide scripts for both Bash and PowerShell to quickly spin up the application with the correct configuration:
- **Linux / macOS / Git Bash**: `./docker-run.sh`
- **Windows (PowerShell)**: `.\docker-run.ps1`

These scripts will:
1. Pull or build the multi-arch container image.
2. Clean up any existing container instances.
3. Start a new container with the default `admin/admin123` credentials.

#### Default Credentials
- **Username**: `admin`
- **Password**: `admin123`

### Configuration Overrides
You can override default settings by passing environment variables:
- `ADMIN_USERNAME`: Administrative account username.
- `ADMIN_PASSWORD`: Administrative account password.
- `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL target properties (when running with production profile).

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
- **[Architectural Decision Index](adr/README.md)** — Complete catalog of ADRs 0001 through 0010.
- **[Frequently Asked Questions](FAQ.md)** — Common questions regarding authentication, rate limiting, and API aggregator routes.
