# ADR 0004: GitHub Actions Node 24 Migration and Explicit SHA Security Pinning

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & DevOps Team
* **Date:** 2026-08-22

---

## 1. Context & Business Problem Statement

GitHub Actions runners are deprecating Node 20 runtimes in favor of Node 24. Workflows referencing outdated major action tags (e.g. `@v4`, `@v2`) risk execution failures, runtime deprecation warnings, and compatibility breaks.

Furthermore, referencing mutable action tags (e.g. `uses: actions/checkout@v4`) exposes CI/CD pipelines to supply-chain attacks and violates security audit rules (such as Semgrep / SAST rules requiring immutable commit SHA references).

---

## 2. Decision Drivers

1. **Node 24 Compatibility**: Upgrade all third-party GitHub Actions to major versions supporting Node 24 runtime without relying on deprecated Node 20 shims.
2. **Supply-Chain Security & Immutability**: Pin every action step to its immutable 40-character commit SHA (e.g., `uses: actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1 # v7.0.1`) to ensure determinism and defend against compromised tag overwrites.
3. **Pipeline Reliability**: Ensure all automated workflows (`auto-bump-version`, `docker-publish`, `nightly-dependency-update`, `pr-validation`, `security-scan`, `semantic-release`, `update-nvd-cache`) run reliably across all triggers.

---

## 3. Decision Outcome

Chosen Strategy: **Upgrade to Node 24-compatible Action Major Versions with Full SHA Pinning**.

### Key Action Upgrades & Pins

| Action | Target Major Version | Immutable Commit SHA |
| :--- | :--- | :--- |
| `actions/checkout` | `@v7.0.1` | `3d3c42e5aac5ba805825da76410c181273ba90b1` |
| `actions/setup-node` | `@v6.2.0` | `249970729cb0ef3589644e2896645e5dc5ba9c38` |
| `actions/setup-java` | `@v5.0.0` | `b6effb05e454b25005698d916606bdc6ffcbf961` |
| `actions/cache/restore` & `save` | `@v5.0.0` | `caa296126883cff596d87d8935842f9db880ef25` |
| `docker/login-action` | `@v4.3.0` | `dbcb813823bdd20940b903addbd779551569679f` |
| `docker/setup-qemu-action` | `@v4.0.0` | `96fe6ef7f33517b61c61be40b68a1882f3264fb8` |
| `docker/setup-buildx-action` | `@v4.3.0` | `37fe631027851001ddb9b187196cc803df7f5f0e` |
| `docker/metadata-action` | `@v6.2.0` | `dc802804100637a589fabce1cb79ff13a1411302` |
| `docker/build-push-action` | `@v7.3.0` | `53b7df96c91f9c12dcc8a07bcb9ccacbed38856a` |
| `gitleaks/gitleaks-action` | `@v3.0.0` | `e0c47f4f8be36e29cdc102c57e68cb5cbf0e8d1e` |
| `softprops/action-gh-release` | `@v3.0.0` | `c12583777ecdfd3be55c69cf75464299dc01057e` |
| `peter-evans/create-pull-request` | `@v7.0.11` | `22a9089034f40e5a961c8808d113e2c98fb63676` |
| `actions/github-script` | `@v8.0.0` | `ed597411d8f924073f98dfc5c65a23a2325f34cd` |

---

## 4. Consequences & Trade-Offs

### Positive
* **Zero Deprecation Warnings**: Prevents build failures caused by Node 20 runner deprecations.
* **Tamper-Proof CI/CD**: SHA pinning protects workflows from tag mutations or upstream compromises.
* **Standardized CI**: All 8 workflow YAML files follow consistent action pinning and Node 24 configurations.

### Trade-Offs
* **Manual SHA Updates**: Bumping actions in the future requires querying git tag SHAs (`git ls-remote`).

---

## 5. Next Steps & Validation

- [x] Query remote git repositories for release tag SHAs
- [x] Update all 8 workflow files under `.github/workflows/`
- [x] Document decision in ADR index
