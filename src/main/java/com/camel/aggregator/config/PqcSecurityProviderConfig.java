package com.camel.aggregator.config;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

/**
 * Registers Bouncy Castle and Post-Quantum Cryptography (PQC) security providers.
 * Supports NIST FIPS 203 (ML-KEM) and FIPS 204 (ML-DSA) algorithms.
 */
@Configuration
public class PqcSecurityProviderConfig {

    private static final Logger log = LoggerFactory.getLogger(PqcSecurityProviderConfig.class);

    @PostConstruct
    public void registerPqcProviders() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.info("[PQC] Registered Bouncy Castle Security Provider ({})", BouncyCastleProvider.PROVIDER_NAME);
        }

        // Try registering Bouncy Castle PQC Provider if present
        try {
            Class<?> pqcProviderClass = Class.forName("org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider");
            java.security.Provider pqcProvider = (java.security.Provider) pqcProviderClass.getDeclaredConstructor().newInstance();
            if (Security.getProvider(pqcProvider.getName()) == null) {
                Security.addProvider(pqcProvider);
                log.info("[PQC] Registered Bouncy Castle PQC Provider ({})", pqcProvider.getName());
            }
        } catch (Exception e) {
            log.debug("[PQC] BouncyCastlePQCProvider not loaded separately or embedded in BC: {}", e.getMessage());
        }

        log.info("[PQC] Post-Quantum Cryptography initialization complete. NIST FIPS 203 ML-KEM baseline active.");
    }
}
