# ADR 0010: Post-Quantum Cryptography Implementation with Bouncy Castle ML-KEM-768

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Team
* **Date:** 2026-09-02
* **Supersedes:** [ADR-0002](0002-post-quantum-crypto-ml-kem-merkle-trees.md)

---

## 1. Context & Business Problem Statement

[ADR-0002](0002-post-quantum-crypto-ml-kem-merkle-trees.md) originally proposed a long-term architectural strategy and roadmap for adopting Post-Quantum Cryptography (PQC) and Merkle Tree Certificates.

With the official finalization of NIST FIPS 203 (Module-Lattice-Based Key-Encapsulation Mechanism Standard / ML-KEM) and Bouncy Castle 1.82 JCA/JCE availability for Java 25, the Enterprise Command Center requires a concrete, active implementation to defend enterprise message streams, REST APIs, and WebSockets against Harvest-Now-Decrypt-Later (HNDL) threats.

---

## 2. Decision Drivers

1. **NIST FIPS 203 Standardization**: Transition from draft algorithms to official ML-KEM-768 (NIST Security Level 3, equivalent to AES-192 security strength).
2. **Java 25 Platform Integration**: Seamless JCA integration with Bouncy Castle 1.82 provider (`bcprov-jdk18on`, `bcpkix-jdk18on`) and Java `javax.crypto.KEM` API.
3. **Operational Telemetry & Observability**: Real-time verification endpoints within Camel REST DSL to monitor PQC provider status and execute live encapsulation/decapsulation cycles.
4. **Backward Compatibility**: Configurable hybrid TLS named groups (`X25519+MLKEM768`) allowing seamless interoperability with classical and hybrid TLS 1.3 clients.

---

## 3. Decision Outcome

Chosen Strategy: **Implement NIST FIPS 203 ML-KEM-768 with Bouncy Castle 1.82 and Camel REST telemetry, superseding the preliminary roadmap in ADR-0002.**

### Architecture & Components

1. **Bouncy Castle PQC Dependencies (`pom.xml`)**:
   - `org.bouncycastle:bcprov-jdk18on:1.82`
   - `org.bouncycastle:bcpkix-jdk18on:1.82`

2. **Security Provider Configuration (`PqcSecurityProviderConfig.java`)**:
   - Automatically registers `BouncyCastleProvider` and `BouncyCastlePQCProvider` on JVM startup.
   - Activates JCA algorithm definitions for ML-KEM, ML-DSA, and hybrid groups.

3. **Key Encapsulation Service (`PostQuantumCryptoService.java`)**:
   - `generateMlKemKeyPair()`: Generates NIST FIPS 203 compliant ML-KEM-768 key pairs.
   - `encapsulate(PublicKey)`: Produces quantum-resilient 1088-byte ciphertexts and 256-bit symmetric shared secrets.
   - `decapsulate(PrivateKey, byte[])`: Recovers shared secrets deterministically.
   - `getPqcStatus()`: Exports algorithm readiness and quantum security level metadata.

4. **Camel REST Integration (`PqcRoute.java`)**:
   - `GET /camel/pqc/status`: Exposes active PQC provider capabilities and supported algorithm matrix.
   - `GET /camel/pqc/demo-exchange`: Executes a live encapsulation/decapsulation verification cycle.

5. **Automated Verification Suite (`PostQuantumCryptoServiceTest.java`)**:
   - Verifies keypair generation, encapsulation, decapsulation, and shared secret equality.

---

## 4. Consequences & Trade-Offs

### Positive Consequences
* **Quantum Decryption Resistance**: All generated shared secrets are immune to Shor's algorithm and quantum cryptanalysis.
* **Deterministic Verification**: Continuous automated testing verifies cryptographic operations in CI/CD.
* **Standards Compliance**: Complies directly with finalized NIST FIPS 203 specifications.

### Negative Consequences & Operational Costs
* **Ciphertext Size**: ML-KEM-768 ciphertexts (~1088 bytes) and public keys (~1184 bytes) are larger than classical X25519 keys (32 bytes).
* **Dependency Footprint**: Adds Bouncy Castle provider libraries (~8MB jar footprint) to the application runtime.

---

## 5. Next Steps & Validation

- [x] Integrate Bouncy Castle 1.82 dependencies in `pom.xml`.
- [x] Implement `PqcSecurityProviderConfig.java` and `PostQuantumCryptoService.java`.
- [x] Implement `PqcRoute.java` Camel REST DSL endpoints.
- [x] Add and pass automated unit tests in `PostQuantumCryptoServiceTest.java`.
- [x] Update ADR-0002 to `Superseded by ADR-0010`.
- [x] Synchronize ADR Index (`tools/adr_index.json` and `docs/adr/README.md`).
