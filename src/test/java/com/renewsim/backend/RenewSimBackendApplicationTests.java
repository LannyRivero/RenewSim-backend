package com.renewsim.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.renewsim.backend.config.TestSecurityConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RenewSimBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
