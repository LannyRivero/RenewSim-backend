package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.infrastructure.config.ActuatorSecurityConfig;
import com.renewsim.backend.auth_service.infrastructure.config.ActuatorSecurityConfigProd;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import static org.assertj.core.api.Assertions.assertThat;

class ActuatorProfileContractTest {

    @Test
    void nonProdActuatorSecurityIsLimitedToLocalDevAndTest() {
        Profile profile = ActuatorSecurityConfig.class.getAnnotation(Profile.class);

        assertThat(profile).isNotNull();
        assertThat(profile.value()).containsExactly("local", "dev", "test");
    }

    @Test
    void prodActuatorSecurityAlsoProtectsStage() {
        Profile profile = ActuatorSecurityConfigProd.class.getAnnotation(Profile.class);

        assertThat(profile).isNotNull();
        assertThat(profile.value()).containsExactly("prod", "production", "stage");
    }
}
