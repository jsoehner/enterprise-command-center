## 🛡️ Cryptographic Bill of Materials (CBOM) & PQC Migration Assessment

**Format**: CycloneDX (v1.7) | **First-Party Code Crypto Assets**: 5 | **Total Tracked Crypto Assets**: 5

### 📊 Post-Quantum Migration Scorecard

| Metric | Count | Migration Status |
|---|---|---|
| **Post-Quantum Ready (PQC)** | **2** | 🟢 Quantum-Resistant (NIST FIPS 203/204/205) |
| **Quantum-Vulnerable (Backlog)** | **2** | 🔴 At Risk of 'Harvest Now, Decrypt Later' |
| **Classical Symmetric / Hashing** | **1** | 🟡 Classical Security (Requires AES-256 / SHA-256+) |
| **Asymmetric PQC Migration Progress** | **50.0%** | (2 of 4 asymmetric primitives migrated) |

### 🎯 Cryptographic Supply Chain Coverage & Confidence

| Evaluation Layer | Coverage / Status | Audit Confidence Assessment |
|---|---|---|
| **First-Party Code (`src/`)** | **100% Audited** (0 Custom Primitives) | 🟢 **HIGH** (Direct AST & SAST verified clean) |
| **Third-Party Supply Chain** | **0.0%** (0 of 2 dependencies cataloged) | 🔴 LOW (Known profiles assimilated) |
| **Overall Audit Confidence Score** | **33.3%** | **🔴 LOW** (2 unassimilated supply chain dependencies) |

### ✅ Post-Quantum Cryptography Migrated Assets

| Component Name | Primitive | Key/Parameter Set | PQC Standard | Provenance / Location(s) |
|---|---|---|---|---|
| `ML-KEM-768` | kem | N/A | NIST FIPS 203 (ML-KEM) | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/config/PqcSecurityProviderConfig.java:39`<br>`src/main/java/com/camel/aggregator/routes/PqcRoute.java:33`<br>`src/main/java/com/camel/aggregator/routes/PqcRoute.java:52`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:29`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:43`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:45`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:46`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:58`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:61`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:63`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:89`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:92`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:94`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:108`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:113`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:114`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:115`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:119`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:121`<br>`src/test/java/com/camel/aggregator/service/PostQuantumCryptoServiceTest.java:23`<br>`src/test/java/com/camel/aggregator/service/PostQuantumCryptoServiceTest.java:29`<br>`src/test/java/com/camel/aggregator/service/PostQuantumCryptoServiceTest.java:59` |
| `ML-DSA-65` | signature | N/A | NIST FIPS 204 (ML-DSA) | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:116`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:117`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:118` |

### ⚠️ Quantum-Vulnerable Assets & Remediation Plan

| Component / Algorithm | Type / Primitive | Key Length / Curve | Recommended Target | Provenance / Context |
|---|---|---|---|---|
| **`ECDH-X25519`**<br><sub>ECDH-X25519</sub> | algorithm / key-agreement | 25519 | **ML-KEM-768 / Kyber (FIPS 203)** | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:119`<br><sub><code>"X25519+MLKEM768 (Hybrid TLS 1.3 Draft Group)"</code></sub><br><br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:121`<br><sub><code>status.put("hybridTlsGroups", List.of("X25519+MLKEM768", "SecP256r1+MLKEM768"));</code></sub> |
| **`ECDSA-P256`**<br><sub>ECDSA-P256</sub> | algorithm / signature | secp256r1 | **ML-DSA-65 / Dilithium (FIPS 204)** | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:121`<br><sub><code>status.put("hybridTlsGroups", List.of("X25519+MLKEM768", "SecP256r1+MLKEM768"));</code></sub> |

### 🔒 Classical Symmetric & Digest Assets

| Component Name | Primitive | Key Length | Quantum Resistance Assessment | Provenance / Location(s) |
|---|---|---|---|---|
| `AES-256-GCM` | block-cipher | 256 | Quantum-Resistant (Grover's proof) | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:111` |

### ⚠️ Unassimilated Third-Party Binaries & Cryptographic Blind Spots

> ℹ️ *The following third-party dependencies do not have verified upstream CBOM attestations in the catalog. They lower the audit confidence score until explicit CBOMs or attestations are published.* 

| Dependency Name | Version | Package URL (purl) | Status |
|---|---|---|---|
| `enterprise-command-center` | N/A | `N/A` | 🟡 Unassimilated (No upstream CBOM) |
| `enterprise-command-center` | latest | `pkg:container/enterprise-command-center@latest` | 🟡 Unassimilated (No upstream CBOM) |
