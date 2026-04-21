package com.renewsim.backend.auth_service.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OtpGenerator domain service")
class OtpGeneratorTest {

    private OtpGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new OtpGenerator();
    }

    @Test
    @DisplayName("generate() returns exactly 6 characters")
    void generate_returnsExactlySixChars() {
        for (int i = 0; i < 50; i++) {
            assertThat(generator.generate()).hasSize(6);
        }
    }

    @Test
    @DisplayName("generate() returns only digit characters")
    void generate_returnsOnlyDigits() {
        for (int i = 0; i < 50; i++) {
            assertThat(generator.generate()).matches("\\d{6}");
        }
    }

    @Test
    @DisplayName("generate() zero-pads codes below 100000")
    void generate_zeroPadsShortCodes() {
        // Any valid output must be exactly 6 digits including leading zeros
        for (int i = 0; i < 100; i++) {
            String code = generator.generate();
            assertThat(code).hasSize(6);
            assertThat(Integer.parseInt(code)).isBetween(0, 999_999);
        }
    }

    @Test
    @DisplayName("generate() produces diverse output across 100 samples")
    void generate_producesDiverseOutput() {
        Set<String> samples = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            samples.add(generator.generate());
        }
        // With 10^6 possible values, 100 samples should yield at least 90 unique codes
        assertThat(samples.size()).isGreaterThanOrEqualTo(90);
    }

    @Test
    @DisplayName("generate() never returns null")
    void generate_neverReturnsNull() {
        for (int i = 0; i < 50; i++) {
            assertThat(generator.generate()).isNotNull();
        }
    }
}