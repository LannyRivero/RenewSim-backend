package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import com.renewsim.backend.simulation_service.shared.application.SimulationProviderTelemetry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class PvgisSolarResourceAdapterTest {

        @Test
        @DisplayName("fetchProfile parses PVGIS responses into a complete profile")
        void fetchProfileParsesResponsesIntoCompleteProfile() {
                RestTemplate restTemplate = new RestTemplate();
                MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
                SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
                PvgisSolarResourceAdapter adapter = new PvgisSolarResourceAdapter(
                                new PvgisProperties("https://re.jrc.ec.europa.eu", null, null),
                                new ObjectMapper(),
                                restTemplate,
                                new SimulationProviderTelemetry(meterRegistry));

                server.expect(requestTo(
                                "https://re.jrc.ec.europa.eu/api/v5_2/PVcalc?lat=37.3891&lon=-5.9845&peakpower=1&loss=14.0&optimalangles=1&outputformat=json"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(pvcalcPayload(), MediaType.APPLICATION_JSON));

                server.expect(requestTo(
                                "https://re.jrc.ec.europa.eu/api/v5_2/tmy?lat=37.3891&lon=-5.9845&outputformat=json"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(tmyPayload(), MediaType.APPLICATION_JSON));

                var profile = adapter.fetchProfile(37.3891, -5.9845, 14.0);

                assertThat(profile.monthlyGenerationPerKwp()).hasSize(12);
                assertThat(profile.monthlyIrradianceKwhM2()).hasSize(12);
                assertThat(profile.monthlyTemperatureC()).hasSize(12);
                assertThat(profile.climatePeriod()).isEqualTo("2005-2020");
                assertThat(profile.source()).isEqualTo("PVGIS");
                assertThat(meterRegistry.counter("simulation_service_provider_calls_total", "provider", "pvgis", "outcome", "success").count())
                                .isEqualTo(1.0d);
                server.verify();
        }

        @Test
        @DisplayName("fetchProfile fails fast when PVGIS response is incomplete")
        void fetchProfileFailsFastWhenResponseIsIncomplete() {
                RestTemplate restTemplate = new RestTemplate();
                MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
                SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
                PvgisSolarResourceAdapter adapter = new PvgisSolarResourceAdapter(
                                new PvgisProperties("https://re.jrc.ec.europa.eu", null, null),
                                new ObjectMapper(),
                                restTemplate,
                                new SimulationProviderTelemetry(meterRegistry));

                server.expect(manyTimes(), requestTo(
                                "https://re.jrc.ec.europa.eu/api/v5_2/PVcalc?lat=37.3891&lon=-5.9845&peakpower=1&loss=14.0&optimalangles=1&outputformat=json"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(
                                                "{\"outputs\":{\"monthly\":{\"fixed\":[]}},\"inputs\":{\"meteo_data\":{\"year_min\":2005,\"year_max\":2020}}}",
                                                MediaType.APPLICATION_JSON));

                server.expect(manyTimes(), requestTo(
                                "https://re.jrc.ec.europa.eu/api/v5_2/tmy?lat=37.3891&lon=-5.9845&outputformat=json"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess("{\"outputs\":{\"tmy_hourly\":[]}}",
                                                MediaType.APPLICATION_JSON));

                assertThatThrownBy(() -> adapter.fetchProfile(37.3891, -5.9845, 14.0))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("PVGIS response is incomplete");
                assertThat(meterRegistry.counter("simulation_service_provider_calls_total", "provider", "pvgis", "outcome", "error").count())
                                .isEqualTo(1.0d);

                server.verify();
        }

        @Test
        @DisplayName("fetchProfile wraps provider failure in an infrastructure exception")
        void fetchProfileWrapsProviderFailure() {
                RestTemplate restTemplate = new RestTemplate();
                MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
                SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
                PvgisSolarResourceAdapter adapter = new PvgisSolarResourceAdapter(
                                new PvgisProperties("https://re.jrc.ec.europa.eu", null, null),
                                new ObjectMapper(),
                                restTemplate,
                                new SimulationProviderTelemetry(meterRegistry));

                server.expect(manyTimes(), requestTo(
                                "https://re.jrc.ec.europa.eu/api/v5_2/PVcalc?lat=37.3891&lon=-5.9845&peakpower=1&loss=14.0&optimalangles=1&outputformat=json"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withServerError());

                assertThatThrownBy(() -> adapter.fetchProfile(37.3891, -5.9845, 14.0))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("Failed to fetch PVGIS solar resource data");
                assertThat(meterRegistry.counter("simulation_service_provider_calls_total", "provider", "pvgis", "outcome", "error").count())
                                .isEqualTo(1.0d);

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
