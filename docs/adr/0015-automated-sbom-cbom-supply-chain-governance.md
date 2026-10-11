# ADR 0015: Automated SBOM and CBOM Supply Chain Governance and Continuous Cryptographic Auditing

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & DevSecOps Team
* **Date:** 2026-10-11

---

## 1. Context and Problem Statement

Modern enterprise software systems face expanding regulatory and supply-chain transparency requirements (e.g., Executive Order 14028, NIST SP 800-218 Secure Software Development Framework, and post-quantum migration mandates under CNSA 2.0 and NIST SP 800-208/FIPS 203/204).

The Enterprise Command Center aggregates multi-tenant enterprise data and incorporates foundational cryptographic primitives, including Bouncy Castle Post-Quantum Cryptography (ML-KEM-768 as codified in [ADR-0010](0010-post-quantum-cryptography-ml-kem-implementation.md)). While automated security scanning exists ([`.github/workflows/security-scan.yml`](../../.github/workflows/security-scan.yml), [ADR-0014](0014-security-scan-vulnerability-remediation-and-sca-suppression-governance.md)), the project lacked:
1. Standardized, machine-readable Software Bill of Materials (SBOM) outputs across industry-standard formats (SPDX and CycloneDX) generated automatically during CI/CD.
2. A formal Cryptographic Bill of Materials (CBOM) capturing classical algorithms, post-quantum algorithms, key sizes, and cipher parameters across both direct dependencies and application source code.
3. Code-level Abstract Syntax Tree (AST) cryptographic reconciliation to identify direct call sites, instantiations, and unmanaged cryptographic primitives.
4. Automated verification harnesses ensuring that generated SBOM and CBOM artifacts conform to valid schemas and retain non-empty component inventories.
5. Automated Post-Quantum Cryptography (PQC) readiness evaluation to measure migration progress against quantum threats (Harvest Now, Decrypt Later).

---

## 2. Decision Drivers & Architecture Significance Requirements (ASRs)

1. **Supply Chain Transparency & Multi-Standard Interoperability**: Support both CycloneDX (v1.5/v1.6) and SPDX (v2.3) SBOM formats for broad ecosystem and regulator interoperability.
2. **Comprehensive Cryptographic Asset Discovery**: Capture cryptographic algorithms, protocols, key lengths, and padding modes across dependencies, container base layers, and source AST call sites.
3. **PQC Governance & Quantum-Readiness Tracking**: Actively track classical algorithms (e.g., RSA, ECDSA, AES-GCM) alongside post-quantum algorithms (ML-KEM-768) to guide zero-trust quantum migration.
4. **CI/CD Pipeline Integrity & Immutability**: Enforce immutable commit SHA pinning on GitHub Actions under Node 24 runners in adherence to [ADR-0004](0004-github-actions-node24-sha-pinning.md).
5. **Developer & Operator Autonomy**: Provide both fully automated CI workflows and reproducible local developer CLI scripts (`scripts/generate_boms.sh`, `scripts/scan_crypto_ast.py`, `scripts/test_boms.sh`, `scripts/analyze_cbom.py`).

---

## 3. Considered Options & Alternatives

| Option | Description | Pros | Cons / Trade-offs |
| :--- | :--- | :--- | :--- |
| **Option 1: Static Manual Bill of Materials** | Maintain static markdown/JSON inventory manifests manually in the repository. | Zero runtime CI overhead; no external tooling dependencies. | High maintenance burden; prone to drift; unverified schema; inadequate for compliance audits. |
| **Option 2: Single-Engine Dependency SBOM Only** | Integrate Anchore Syft only for package dependencies without CBOM or AST analysis. | Easy setup; standard SBOM output. | Blind to cryptographic primitives, cipher configurations, algorithm parameters, and PQC transition metrics. |
| **Option 3: Integrated Dual-Engine SBOM/CBOM + AST Analysis Pipeline (Chosen)** | Combine Anchore/Syft (CycloneDX/SPDX), `cbomkit` action, Python AST call-site extractor, automated verification harness, and PQC dashboard. | Full supply chain coverage; deep cryptographic inventory; automated schema and content verification; PQC migration scorecards. | Introduces CI pipeline steps and script maintenance in `scripts/`. |

---

## 4. Decision Outcome

We decided to adopt **Option 3: Integrated Dual-Engine SBOM/CBOM + AST Analysis Pipeline** across the Enterprise Command Center repository.

### Implementation Details

1. **GitHub Actions Workflow ([`.github/workflows/sbom-cbom.yml`](../../.github/workflows/sbom-cbom.yml))**:
   - Executes on push to `main`/`master`, tags, pull requests, and manual triggers (`workflow_dispatch`).
   - Uses Node 24 compatible actions pinned to immutable 40-character commit SHAs:
     - `actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1` (`v7.0.1`)
     - `anchore/sbom-action@006b7ce8314066bdf1765b4500370d40fa6917a3` (`v0.24.2`) for CycloneDX and SPDX SBOMs
     - `cbomkit/cbomkit-action@34a3d960625ae7f7f1b59455eff93a96df090eb7` (`v2.3.0`) for CycloneDX CBOM
     - `actions/setup-python@a26af69be951a213d495a4c3e4e4022e16d87065` (`v5.6.0`)
     - `actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02` (`v4.6.2`)
2. **Dual-Engine Semantic AST Cryptographic Scan ([`scripts/scan_crypto_ast.py`](../../scripts/scan_crypto_ast.py))**:
   - Inspects application source code across Java, Python, Go, and TypeScript/JavaScript for cryptographic instantiation patterns.
   - Extracts exact file paths, line numbers, and algorithm identifiers.
   - Reconciles findings into the CycloneDX CBOM, ensuring application call sites are represented alongside third-party libraries.
3. **Automated Verification Test Harness ([`scripts/test_boms.sh`](../../scripts/test_boms.sh))**:
   - Validates JSON parsing and schema structure of generated SBOM and CBOM artifacts.
   - Verifies minimum component and cryptographic asset thresholds to fail-fast on empty or corrupted outputs.
4. **PQC Readiness Analysis & Audit Reporting ([`scripts/analyze_cbom.py`](../../scripts/analyze_cbom.py))**:
   - Analyzes CBOM assets against post-quantum criteria, identifying classical versus quantum-resilient algorithms.
   - Publishes step summaries and updates [`docs/security/CRYPTOGRAPHIC_AUDIT.md`](../../docs/security/CRYPTOGRAPHIC_AUDIT.md) on workflow execution.
5. **Local Developer Automation**:
   - Provided standalone CLI wrappers in [`scripts/`](../../scripts/): `generate_boms.sh`, `attach_oci_bom.sh`, `fetch_oci_cbom.py`.
   - Ignored transient generation outputs via `oss/` in [`.gitignore`](../../.gitignore).

---

## 5. Consequences & Trade-offs

### Positive Consequences
* **Automated Compliance Readiness**: Satisfies modern US/EU regulatory and federal SBOM/CBOM mandates with standard CycloneDX and SPDX outputs.
* **Deep Cryptographic Visibility**: Real-time tracking of cryptographic primitives, eliminating hidden or forgotten weak cipher implementations.
* **PQC Migration Visibility**: Direct metrics on ML-KEM adoption progress vs. legacy classical encryption algorithms.
* **Hermetic and Secure CI**: All workflow dependencies are pinned to immutable commit hashes under Node 24 standards.

### Negative Consequences & Operational Costs
* **CI Execution Overhead**: Adds approximately 1–2 minutes to automated pipeline runs for artifact extraction and AST analysis.
* **Script Maintenance**: Internal scripts in `scripts/` must be maintained alongside core build tooling.

### Follow-on Actions & Operational Checklist
- [x] Install SBOM/CBOM workflow (`.github/workflows/sbom-cbom.yml`) and support scripts.
- [x] Add `oss/` build artifact directory to `.gitignore`.
- [x] Register ADR-0015 in `docs/adr/README.md` and `tools/adr_index.json`.
- [x] Document pipeline usage, CI stages, and local generation commands in `README.md`.
- [ ] Monitor CI execution on `main` branch push and verify upload of `bom-artifacts`.
