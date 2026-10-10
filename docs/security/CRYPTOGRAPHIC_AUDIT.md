## 🛡️ Cryptographic Bill of Materials (CBOM) & PQC Migration Assessment

**Format**: CycloneDX (v1.6) | **First-Party Code Crypto Assets**: 7 | **Total Tracked Crypto Assets**: 7

### 📊 Post-Quantum Migration Scorecard

| Metric | Count | Migration Status |
|---|---|---|
| **Post-Quantum Ready (PQC)** | **2** | 🟢 Quantum-Resistant (NIST FIPS 203/204/205) |
| **Quantum-Vulnerable (Backlog)** | **2** | 🔴 At Risk of 'Harvest Now, Decrypt Later' |
| **Classical Symmetric / Hashing** | **3** | 🟡 Classical Security (Requires AES-256 / SHA-256+) |
| **Asymmetric PQC Migration Progress** | **50.0%** | (2 of 4 asymmetric primitives migrated) |

### 🎯 Cryptographic Supply Chain Coverage & Confidence

| Evaluation Layer | Coverage / Status | Audit Confidence Assessment |
|---|---|---|
| **First-Party Code (`src/`)** | **100% Audited** (0 Custom Primitives) | 🟢 **HIGH** (Direct AST & SAST verified clean) |
| **Third-Party Supply Chain** | **1.1%** (1 of 93 dependencies cataloged) | 🔴 LOW (Known profiles assimilated) |
| **Overall Audit Confidence Score** | **2.1%** | **🔴 LOW** (92 unassimilated supply chain dependencies) |

### ✅ Post-Quantum Cryptography Migrated Assets

| Component Name | Primitive | Key/Parameter Set | PQC Standard | Provenance / Location(s) |
|---|---|---|---|---|
| `ML-KEM-768` | kem | 768 | NIST FIPS 203 (ML-KEM) | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java`<br>`src/test/java/com/camel/aggregator/service/PostQuantumCryptoServiceTest.java:23`<br>`src/test/java/com/camel/aggregator/service/PostQuantumCryptoServiceTest.java:29`<br>`src/test/java/com/camel/aggregator/service/PostQuantumCryptoServiceTest.java:59`<br>`src/main/java/com/camel/aggregator/config/PqcSecurityProviderConfig.java:39`<br>`src/main/java/com/camel/aggregator/routes/PqcRoute.java:33`<br>`src/main/java/com/camel/aggregator/routes/PqcRoute.java:52`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:29`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:43`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:45`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:46`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:58`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:61`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:63`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:89`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:92`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:94`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:108`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:113`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:114`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:115`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:119`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:121` |
| `ML-DSA-65` | signature | N/A | NIST FIPS 204 (ML-DSA) | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:116`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:117`<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:118` |

### ⚠️ Quantum-Vulnerable Assets & Remediation Plan

| Component / Algorithm | Type / Primitive | Key Length / Curve | Recommended Target | Provenance / Context |
|---|---|---|---|---|
| **`ECDH-X25519`**<br><sub>ECDH-X25519</sub> | algorithm / key-agreement | 25519 | **ML-KEM-768 / Kyber (FIPS 203)** | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:119`<br><sub><code>"X25519+MLKEM768 (Hybrid TLS 1.3 Draft Group)"</code></sub><br><br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:121`<br><sub><code>status.put("hybridTlsGroups", List.of("X25519+MLKEM768", "SecP256r1+MLKEM768"));</code></sub> |
| **`ECDSA-P256`**<br><sub>ECDSA-P256</sub> | algorithm / signature | secp256r1 | **ML-DSA-65 / Dilithium (FIPS 204)** | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:121`<br><sub><code>status.put("hybridTlsGroups", List.of("X25519+MLKEM768", "SecP256r1+MLKEM768"));</code></sub> |

### 🔒 Classical Symmetric & Digest Assets

| Component Name | Primitive | Key Length | Quantum Resistance Assessment | Provenance / Location(s) |
|---|---|---|---|---|
| `key@4db67034-4e50-4300-a627-0a5f22bfb368` | unknown | N/A | Review key length for Grover resistance | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java` |
| `key@851ae29c-3731-47c7-b6b5-ae36f4c2e214` | unknown | N/A | Review key length for Grover resistance | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java` |
| `AES-256-GCM` | block-cipher | 256 | Quantum-Resistant (Grover's proof) | First-Party Code (SAST/AST)<br>`src/main/java/com/camel/aggregator/service/PostQuantumCryptoService.java:111` |

### ⚠️ Unassimilated Third-Party Binaries & Cryptographic Blind Spots

> ℹ️ *The following third-party dependencies do not have verified upstream CBOM attestations in the catalog. They lower the audit confidence score until explicit CBOMs or attestations are published.* 

| Dependency Name | Version | Package URL (purl) | Status |
|---|---|---|---|
| `actions/cache/restore` | v5.0.0 | `pkg:github/actions/cache@v5.0.0#restore` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/cache/save` | v5.0.0 | `pkg:github/actions/cache@v5.0.0#save` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/checkout` | v7.0.1 | `pkg:github/actions/checkout@v7.0.1` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/github-script` | v8.0.0 | `pkg:github/actions/github-script@v8.0.0` | 🟡 Unassimilated (No upstream CBOM) |
| `actions/setup-java` | v5.0.0 | `pkg:github/actions/setup-java@v5.0.0` | 🟡 Unassimilated (No upstream CBOM) |
| *... and 77 more unassimilated dependencies* | | | |
