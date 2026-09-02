package com.camel.aggregator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PostQuantumCryptoServiceTest {

    private PostQuantumCryptoService pqcService;

    @BeforeEach
    void setUp() {
        pqcService = new PostQuantumCryptoService();
    }

    @Test
    @DisplayName("Should generate valid ML-KEM-768 KeyPair")
    void testGenerateMlKemKeyPair() throws Exception {
        KeyPair keyPair = pqcService.generateMlKemKeyPair();
        assertThat(keyPair).isNotNull();
        assertThat(keyPair.getPublic()).isNotNull();
        assertThat(keyPair.getPrivate()).isNotNull();
        assertThat(keyPair.getPublic().getAlgorithm()).containsIgnoringCase("ML-KEM");
    }

    @Test
    @DisplayName("Should encapsulate and decapsulate shared secret with exact match")
    void testEncapsulateAndDecapsulate() throws Exception {
        // 1. Generate KeyPair
        KeyPair keyPair = pqcService.generateMlKemKeyPair();

        // 2. Encapsulate
        Map<String, String> encResult = pqcService.encapsulate(keyPair.getPublic());
        assertThat(encResult).containsKey("ciphertext");
        assertThat(encResult).containsKey("sharedSecret");

        byte[] ciphertext = Base64.getDecoder().decode(encResult.get("ciphertext"));
        assertThat(ciphertext).isNotEmpty();

        // 3. Decapsulate
        String recoveredSharedSecret = pqcService.decapsulate(keyPair.getPrivate(), ciphertext);
        assertThat(recoveredSharedSecret).isNotEmpty();

        // 4. Verify shared secret match
        assertThat(recoveredSharedSecret).isEqualTo(encResult.get("sharedSecret"));
    }

    @Test
    @DisplayName("Should return active PQC status and supported algorithms")
    void testGetPqcStatus() {
        Map<String, Object> status = pqcService.getPqcStatus();
        assertThat(status).containsEntry("status", "ACTIVE");
        assertThat(status).containsEntry("defaultKemAlgorithm", "ML-KEM-768");
        assertThat(status.get("supportedAlgorithms")).asList().isNotEmpty();
    }
}
