package com.renewsim.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

class ContextLoadsProfilesITest {

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
    @ActiveProfiles("test")
    @DisplayName("Context loads with 'test' profile")
    class TestProfile {
        @Test
        void contextLoads() {}
    }
}