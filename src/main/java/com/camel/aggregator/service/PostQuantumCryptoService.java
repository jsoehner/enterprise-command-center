package com.camel.aggregator.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.spec.MLKEMParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.KEM;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing Post-Quantum Cryptography operations, focusing on NIST FIPS 203 (ML-KEM)
 * for quantum-resilient key encapsulation and shared secret establishment.
 */
@Service
public class PostQuantumCryptoService {

    private static final Logger log = LoggerFactory.getLogger(PostQuantumCryptoService.class);
    private static final String DEFAULT_ALGORITHM = "ML-KEM-768";

    public PostQuantumCryptoService() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generates a new ML-KEM-768 KeyPair.
     */
    public KeyPair generateMlKemKeyPair() throws Exception {
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("ML-KEM-768", "BC");
        } catch (Exception e) {
            kpg = KeyPairGenerator.getInstance("ML-KEM", "BC");
            kpg.initialize(MLKEMParameterSpec.ml_kem_768);
        }
        return kpg.generateKeyPair();
    }

    /**
     * Performs KEM Encapsulation using the recipient's public key.
     * Returns a map containing the base64-encoded ciphertext (encapsulation) and shared secret.
     */
    public Map<String, String> encapsulate(PublicKey publicKey) throws Exception {
        KEM kem;
        try {
            kem = KEM.getInstance("ML-KEM", "BC");
        } catch (Exception e) {
            try {
                kem = KEM.getInstance("ML-KEM");
            } catch (Exception ex) {
                kem = KEM.getInstance("ML-KEM-768");
            }
        }
        KEM.Encapsulator encapsulator = kem.newEncapsulator(publicKey);
        KEM.Encapsulated encapsulated = encapsulator.encapsulate();

        byte[] ciphertext = encapsulated.encapsulation();
        SecretKey secretKey = encapsulated.key();

        Map<String, String> result = new LinkedHashMap<>();
        result.put("algorithm", DEFAULT_ALGORITHM);
        result.put("ciphertext", Base64.getEncoder().encodeToString(ciphertext));
        result.put("sharedSecret", Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        result.put("ciphertextSizeBytes", String.valueOf(ciphertext.length));
        result.put("sharedSecretSizeBytes", String.valueOf(secretKey.getEncoded().length));

        return result;
    }

    /**
     * Performs KEM Decapsulation using the recipient's private key and incoming ciphertext.
     * Returns the base64-encoded recovered shared secret.
     */
    public String decapsulate(PrivateKey privateKey, byte[] ciphertext) throws Exception {
        KEM kem;
        try {
            kem = KEM.getInstance("ML-KEM", "BC");
        } catch (Exception e) {
            try {
                kem = KEM.getInstance("ML-KEM");
            } catch (Exception ex) {
                kem = KEM.getInstance("ML-KEM-768");
            }
        }
        KEM.Decapsulator decapsulator = kem.newDecapsulator(privateKey);
        SecretKey secretKey = decapsulator.decapsulate(ciphertext);
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * Returns system-wide PQC readiness and status telemetry.
     */
    public Map<String, Object> getPqcStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "ACTIVE");
        status.put("standard", "NIST FIPS 203 (ML-KEM)");
        status.put("defaultKemAlgorithm", DEFAULT_ALGORITHM);
        status.put("provider", "BouncyCastle Security Provider v1.82");
        status.put("quantumSecurityLevel", "NIST Level 3 (equivalent to AES-192 security)");
        status.put("supportedAlgorithms", List.of(
            "ML-KEM-512 (NIST Level 1)",
            "ML-KEM-768 (NIST Level 3 - Enterprise Baseline)",
            "ML-KEM-1024 (NIST Level 5)",
            "ML-DSA-44",
            "ML-DSA-65",
            "ML-DSA-87",
            "X25519+MLKEM768 (Hybrid TLS 1.3 Draft Group)"
        ));
        status.put("hybridTlsGroups", List.of("X25519+MLKEM768", "SecP256r1+MLKEM768"));
        return status;
    }
}
