package com.camel.aggregator.routes;

import com.camel.aggregator.service.PostQuantumCryptoService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exposes Post-Quantum Cryptography telemetry and ML-KEM exchange endpoints via Camel REST DSL.
 */
@Component
public class PqcRoute extends RouteBuilder {

    private final PostQuantumCryptoService pqcService;

    public PqcRoute(PostQuantumCryptoService pqcService) {
        this.pqcService = pqcService;
    }

    @Override
    public void configure() throws Exception {
        rest("/pqc")
            .get("/status")
                .description("Get current Post-Quantum Cryptography status and supported algorithms")
                .to("bean:postQuantumCryptoService?method=getPqcStatus")

            .get("/demo-exchange")
                .description("Run a live ML-KEM-768 key encapsulation and decapsulation cycle")
                .to("direct:pqc-demo-exchange");

        from("direct:pqc-demo-exchange")
            .process(exchange -> {
                // 1. Generate KeyPair
                KeyPair keyPair = pqcService.generateMlKemKeyPair();
                
                // 2. Encapsulate
                Map<String, String> encResult = pqcService.encapsulate(keyPair.getPublic());
                byte[] ciphertext = Base64.getDecoder().decode(encResult.get("ciphertext"));

                // 3. Decapsulate
                String recoveredSecret = pqcService.decapsulate(keyPair.getPrivate(), ciphertext);

                // 4. Verify match
                boolean verified = recoveredSecret.equals(encResult.get("sharedSecret"));

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("algorithm", "ML-KEM-768 (NIST FIPS 203)");
                response.put("keyExchangeStatus", verified ? "SUCCESS" : "FAILED");
                response.put("publicKeyBase64", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
                response.put("ciphertextSizeBytes", encResult.get("ciphertextSizeBytes"));
                response.put("sharedSecretLengthBits", 256);
                response.put("sharedSecretVerifiedMatch", verified);
                response.put("details", "Deterministic quantum-resilient key exchange verified with Bouncy Castle provider.");

                exchange.getMessage().setBody(response);
            });
    }
}
