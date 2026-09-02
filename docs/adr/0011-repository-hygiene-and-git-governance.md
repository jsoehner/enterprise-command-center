# ADR 0011: Repository Hygiene, Artifact Sanitization, and Git Governance Standards

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Team
* **Date:** 2026-09-02

---

## 1. Context & Business Problem Statement

Over progressive iterations of feature delivery, security auditing, and automated dependency updates, the repository accumulated auxiliary scratch files, legacy autonomous agent traces (`piolium/` runs and attack surface scans), one-off python migration scripts, and root-level notes.

While useful during transient execution, committing uncurated logs, debug transcripts, and editor metadata introduces repository bloat, potential secret/credential leakage risks, and Docker build context overhead.

A formalized repository hygiene strategy, centralized knowledge archive, and comprehensive `.gitignore` policy are required to enforce codebase cleanliness.

---

## 2. Decision Drivers

1. **Supply Chain & Leak Prevention**: Eliminate transient scan reports (`findings-table.md`, `trivy-results.json`, `audit-state.json`) and agent transcripts (`piolium/tmp/`) from source control.
2. **Deterministic CI/CD & Fast Docker Contexts**: Prevent unversioned artifacts and logs from triggering unnecessary Docker layer cache invalidations.
3. **Structured Knowledge Organization**: Migrate actionable technical learnings into dedicated directories (`docs/lessons-learned/`, `docs/DEVELOPER_GUIDE.md`) rather than loose root files.
4. **Developer Experience & Tooling Hygiene**: Support multi-language developers (Java 25, Python 3.14, Node 24) with robust `.gitignore` rules.

---

## 3. Decision Outcome

Chosen Strategy: **Enforce a comprehensive repository sanitization protocol, centralize developer documentation, and establish a multi-tier `.gitignore` standard.**

### Key Architectural Actions

1. **Artifact & Agent Trace Sanitization**:
   - Pruned all historical `piolium/` agent runs, JSON Lines (`*.jsonl`), error logs, and debate logs from source control.
   - Removed temporary audit scripts, obsolete root reports (`security_report.md`, `plan.md`), and Python bytecode caches (`__pycache__`).

2. **Knowledge Base Reorganization**:
   - Structured persistent developer insights under `docs/lessons-learned/`:
     - `docs/lessons-learned/code-cleanup-lessons.md`
     - `docs/lessons-learned/nightly-dependencies-lessons.md`
   - Expanded [`docs/DEVELOPER_GUIDE.md`](../DEVELOPER_GUIDE.md) and [`README.md`](../../README.md) with explicit repository hygiene commands.

3. **Multi-Ecosystem `.gitignore` Architecture**:
   - **Java/Maven**: Strict ignore on `target/`, `*.class`, `*.jar`, JVM crash logs (`hs_err_pid*`), and Maven timing metadata.
   - **Python**: Universal ignore on `__pycache__/`, `*.pyc`, `*.pyo`, virtual environments (`venv/`, `.venv/`), and pytest caches.
   - **Node.js**: Ignore `node_modules/`, `npm-debug.log*`, and package manager caches.
   - **IDEs & OS**: Standardized exclusions for `.idea/`, `.vscode/`, `.DS_Store`, and `Thumbs.db`.
   - **Agents & Scanners**: Strict exclusions for `/piolium/`, `/.pi/`, `*.jsonl`, and intermediate scan outputs.

---

## 4. Consequences & Trade-Offs

### Positive Consequences
* **Streamlined Repository Size**: Drastically reduced tracked file count by eliminating over 70 stale files.
* **Faster Container Builds**: Lean `.dockerignore` and workspace root accelerate Docker build context transfers.
* **Enhanced Security Posture**: Prevents accidental commits of sensitive local run logs or security audit outputs.

### Negative Consequences & Operational Costs
* Developers must explicitly save long-term operational guides into `docs/` rather than placing informal notes in the repository root.

---

## 5. Next Steps & Validation

- [x] Prune stale files, agent directories, and transient logs from git tracking.
- [x] Implement comprehensive `.gitignore` across Maven, Python, Node, IDE, and Agent tools.
- [x] Consolidate technical lessons into `docs/lessons-learned/`.
- [x] Update `docs/DEVELOPER_GUIDE.md` and `README.md`.
- [x] Register ADR 0011 in `tools/adr_index.json` and `docs/adr/README.md`.
