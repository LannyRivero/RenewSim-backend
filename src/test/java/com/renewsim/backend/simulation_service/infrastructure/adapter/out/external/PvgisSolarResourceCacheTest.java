package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.config.CacheConfig;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import com.renewsim.backend.simulation_service.shared.application.SimulationProviderTelemetry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = {CacheConfig.class, PvgisSolarResourceCacheTest.TestConfig.class})
@ActiveProfiles("test")
class PvgisSolarResourceCacheTest {

    @Configuration
    static class TestConfig {
        @Bean
        RestTemplate pvgisRestTemplate() {
            return new RestTemplate();
        }

        @Bean
        PvgisProperties pvgisProperties() {
            return new PvgisProperties("https://re.jrc.ec.europa.eu", null, null);
        }

        @Bean
        SimulationProviderTelemetry simulationProviderTelemetry() {
            return new SimulationProviderTelemetry(new SimpleMeterRegistry());
        }

        @Bean
        PvgisSolarResourceAdapter pvgisSolarResourceAdapter(
                PvgisProperties properties,
                RestTemplate pvgisRestTemplate,
                SimulationProviderTelemetry telemetry) {
            return new PvgisSolarResourceAdapter(properties, new ObjectMapper(), pvgisRestTemplate, telemetry);
        }
    }

    @Autowired
    private RestTemplate pvgisRestTemplate;

    @Autowired
    private PvgisSolarResourcePort pvgisSolarResourceAdapter;

    @Autowired
    private CacheManager cacheManager;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.createServer(pvgisRestTemplate);
        var cache = cacheManager.getCache("simulationSolarResourceProfiles");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("fetchProfile uses cache on second identical call")
    void fetchProfileUsesCacheOnSecondIdenticalCall() {
        server.expect(requestTo("https://re.jrc.ec.europa.eu/api/v5_2/PVcalc?lat=37.3891&lon=-5.9845&peakpower=1&loss=14.0&optimalangles=1&outputformat=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(pvcalcPayload(), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://re.jrc.ec.europa.eu/api/v5_2/tmy?lat=37.3891&lon=-5.9845&outputformat=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(tmyPayload(), MediaType.APPLICATION_JSON));

        var first = pvgisSolarResourceAdapter.fetchProfile(37.3891, -5.9845, 14.0);
        var second = pvgisSolarResourceAdapter.fetchProfile(37.3891, -5.9845, 14.0);

        assertThat(first.climatePeriod()).isEqualTo("2005-2020");
        assertThat(second.climatePeriod()).isEqualTo("2005-2020");
        server.verify();
    }

    @Test
    @DisplayName("profileKey keeps close PVGIS inputs distinct")
    void profileKeyKeepsCloseInputsDistinct() {
        server.expect(requestTo("https://re.jrc.ec.europa.eu/api/v5_2/PVcalc?lat=37.3891&lon=-5.9845&peakpower=1&loss=14.001&optimalangles=1&outputformat=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(pvcalcPayload(), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://re.jrc.ec.europa.eu/api/v5_2/tmy?lat=37.3891&lon=-5.9845&outputformat=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(tmyPayload(), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://re.jrc.ec.europa.eu/api/v5_2/PVcalc?lat=37.3891&lon=-5.9845&peakpower=1&loss=14.004&optimalangles=1&outputformat=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(pvcalcPayload(), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://re.jrc.ec.europa.eu/api/v5_2/tmy?lat=37.3891&lon=-5.9845&outputformat=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(tmyPayload(), MediaType.APPLICATION_JSON));

        var first = pvgisSolarResourceAdapter.fetchProfile(37.3891, -5.9845, 14.001d);
        var second = pvgisSolarResourceAdapter.fetchProfile(37.3891, -5.9845, 14.004d);

        assertThat(first.climatePeriod()).isEqualTo("2005-2020");
        assertThat(second.climatePeriod()).isEqualTo("2005-2020");
        server.verify();
    }

    private String pvcalcPayload() {
        return """
                {
                  "outputs": {
                    "monthly": {
                      "fixed": [
                        {"E_m":100.0,"H(i)_m":10.0},
                        {"E_m":101.0,"H(i)_m":11.0},
                        {"E_m":102.0,"H(i)_m":12.0},
                        {"E_m":103.0,"H(i)_m":13.0},
                        {"E_m":104.0,"H(i)_m":14.0},
                        {"E_m":105.0,"H(i)_m":15.0},
                        {"E_m":106.0,"H(i)_m":16.0},
                        {"E_m":107.0,"H(i)_m":17.0},
                        {"E_m":108.0,"H(i)_m":18.0},
                        {"E_m":109.0,"H(i)_m":19.0},
                        {"E_m":110.0,"H(i)_m":20.0},
                        {"E_m":111.0,"H(i)_m":21.0}
                      ]
                    }
                  },
                  "inputs": {
                    "meteo_data": {
                      "year_min": 2005,
                      "year_max": 2020
                    }
                  }
                }
                """;
    }

    private String tmyPayload() {
        return """
                {
                  "outputs": {
                    "tmy_hourly": [
                      {"time(UTC)":"2024010100","T2m":10.0},
                      {"time(UTC)":"2024020100","T2m":11.0},
                      {"time(UTC)":"2024030100","T2m":12.0},
                      {"time(UTC)":"2024040100","T2m":13.0},
                      {"time(UTC)":"2024050100","T2m":14.0},
                      {"time(UTC)":"2024060100","T2m":15.0},
                      {"time(UTC)":"2024070100","T2m":16.0},
                      {"time(UTC)":"2024080100","T2m":17.0},
                      {"time(UTC)":"2024090100","T2m":18.0},
                      {"time(UTC)":"2024100100","T2m":19.0},
                      {"time(UTC)":"2024110100","T2m":20.0},
                      {"time(UTC)":"2024120100","T2m":21.0}
                    ]
                  }
                }
                """;
    }
}
