package com.camel.aggregator.config;

import com.camel.aggregator.model.User;
import com.camel.aggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DatabaseInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.core.env.Environment environment;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            boolean isProd = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
            if (isProd && ("admin123".equals(adminPassword) || "admin".equalsIgnoreCase(adminUsername))) {
                throw new IllegalStateException("CRITICAL SECURITY VIOLATION: Default admin credentials detected in production profile ('prod'). ADMIN_PASSWORD and ADMIN_USERNAME must be set securely.");
            }
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), "ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("Default admin user created.");
            }
        };
    }
}
