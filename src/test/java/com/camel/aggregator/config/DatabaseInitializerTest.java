package com.camel.aggregator.config;

import com.camel.aggregator.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DatabaseInitializerTest {

    @Test
    void testProductionProfileRejectsDefaultPassword() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        ReflectionTestUtils.setField(initializer, "environment", env);
        ReflectionTestUtils.setField(initializer, "adminUsername", "admin");
        ReflectionTestUtils.setField(initializer, "adminPassword", "admin123");

        CommandLineRunner runner = initializer.initDatabase();
        assertThrows(IllegalStateException.class, () -> runner.run());
    }

    @Test
    void testProductionProfileAllowsSecureCredentials() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        Environment env = mock(Environment.class);
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(userRepo.findByUsername("secureAdmin")).thenReturn(Optional.empty());
        when(encoder.encode("SuperSecretP@ssword2026!")).thenReturn("encodedHash");

        ReflectionTestUtils.setField(initializer, "environment", env);
        ReflectionTestUtils.setField(initializer, "userRepository", userRepo);
        ReflectionTestUtils.setField(initializer, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(initializer, "adminUsername", "secureAdmin");
        ReflectionTestUtils.setField(initializer, "adminPassword", "SuperSecretP@ssword2026!");

        CommandLineRunner runner = initializer.initDatabase();
        assertDoesNotThrow(() -> runner.run());
        verify(userRepo, times(1)).save(any());
    }

    @Test
    void testDevProfileAllowsDefaultCredentials() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        Environment env = mock(Environment.class);
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(env.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(userRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(encoder.encode("admin123")).thenReturn("encodedHash");

        ReflectionTestUtils.setField(initializer, "environment", env);
        ReflectionTestUtils.setField(initializer, "userRepository", userRepo);
        ReflectionTestUtils.setField(initializer, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(initializer, "adminUsername", "admin");
        ReflectionTestUtils.setField(initializer, "adminPassword", "admin123");

        CommandLineRunner runner = initializer.initDatabase();
        assertDoesNotThrow(() -> runner.run());
        verify(userRepo, times(1)).save(any());
    }
}
