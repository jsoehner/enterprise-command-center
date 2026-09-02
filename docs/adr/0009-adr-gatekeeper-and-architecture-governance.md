# ADR 0009: ADR Gatekeeper and Architectural Governance Automation

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & DevOps Team
* **Date:** 2026-09-02

---

## 1. Context & Business Problem Statement

As the Enterprise Command Center grows across multiple integration domains (telemetry, order processing, settlement, logistics, and post-quantum security), tracking architectural choices and preventing architectural drift becomes critical.

Previously, Architectural Decision Records (ADRs) were maintained manually without automated verification or gatekeeping. This posed several operational and governance risks:
1. **Architectural Drift & Silent Creep**: Structural changes (e.g., changes to core Spring configs, database schemas, security filters, or dependency baselines) could be merged without documenting the rationale and trade-offs.
2. **Index Inconsistency**: Manual maintenance of ADR indices (`docs/adr/README.md`) was prone to stale links, missing records, or outdated statuses.
3. **Lack of Automated CI/CD Verification**: Pull requests lacked automated checks to evaluate whether introduced changes constituted Architecturally Significant Requirements (ASRs) requiring an ADR.

---

## 2. Decision Drivers

1. **Continuous Governance**: Automatically detect when pull requests or code modifications touch architecturally significant files or introduce structural patterns.
2. **Standardized Authoring & Lifecycle**: Provide standardized templates (Minimum, Standard, Comprehensive) and enforce strict immutability and bidirectional linking for superseded decisions.
3. **Zero-Friction Developer Experience**: Provide CLI tools (`scripts/adr_gatekeeper.py`), local pre-commit hooks, and GitHub Actions CI validation that evaluate changes deterministically.
4. **Machine-Readable Registry**: Maintain a single-source-of-truth JSON index (`tools/adr_index.json`) synchronized with markdown documentation.

---

## 3. Decision Outcome

Chosen Strategy: **Deploy the Automated ADR Gatekeeper & Decision Registry System**.

### Core Architecture & Components

1. **Analysis Engine (`scripts/adr_gatekeeper.py`)**:
   - Evaluates git staged files, PR diffs, and text descriptions against weighted significance rules.
   - Reindexes all records in `docs/adr/` and synchronizes `tools/adr_index.json` and `docs/adr/README.md`.
   - Verifies integrity of all ADRs (required sections: Context, Decision, Consequences, and bidirectional link validation).

2. **Significance Configuration (`tools/adr_analyst_config.json`)**:
   - Defines scoring weights for critical file patterns (`pom.xml`, `Dockerfile`, `.github/workflows/**`, `src/main/resources/application*.yml`, `src/main/java/**/security/**`, etc.).
   - Defines keyword triggers for cryptography, persistence, protocol changes, and runtime shifts.
   - Maps score thresholds to recommended ADR templates:
     - **Score ≥ 70**: Comprehensive ADR
     - **Score ≥ 40**: Standard ADR
     - **Score ≥ 20**: Minimum ADR

3. **CI/CD & Hook Integration**:
   - **GitHub Actions (`.github/workflows/adr-gatekeeper.yml`)**: Validates ADR integrity and scans PR diffs against architectural thresholds.
   - **Local Hook (`.git/hooks/pre-commit`)**: Validates ADR structural integrity and advises developers on staged architectural impact.

4. **Standardized Templates (`docs/adr/templates/`)**:
   - Minimum ADR Template (`Minimum_ADR_Template.md`)
   - Standard ADR Template (`Standard_ADR_Template.md`)
   - Comprehensive ADR Template (`Comprehensive_ADR_Template.md`)

---

## 4. Consequences & Trade-Offs

### Positive Consequences
* **Automated Guardrails**: Prevents unreviewed architectural changes from entering production.
* **Synchronized Registry**: Eliminates manual maintenance of ADR indices and cross-references.
* **Deterministic Scoring**: Transparent, reproducible evaluation of architectural significance.

### Negative Consequences / Trade-offs
* **Maintenance Overhead**: The rules in `tools/adr_analyst_config.json` must be curated as the system topology evolves.
* **Minor CI Latency**: Adds a lightweight Python analysis step (~1-2 seconds) to pull request validation.

---

## 5. Next Steps & Validation

- [x] Install `scripts/adr_gatekeeper.py` and configuration artifacts.
- [x] Integrate `.github/workflows/adr-gatekeeper.yml` CI workflow.
- [x] Configure `.git/hooks/pre-commit` local git hook.
- [x] Verify existing ADR suite integrity (ADR 0001–0006).
- [x] Synchronize decision index (`tools/adr_index.json` and `docs/adr/README.md`).
