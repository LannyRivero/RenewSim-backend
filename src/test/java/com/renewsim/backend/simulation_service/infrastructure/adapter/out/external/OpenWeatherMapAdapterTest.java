package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.renewsim.backend.simulation_service.shared.application.SimulationProviderTelemetry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.renewsim.backend.simulation_service.infrastructure.config.WeatherServiceProperties;

class OpenWeatherMapAdapterTest {

    @Test
    @DisplayName("searchLocations keeps only supported Spain results")
    void searchLocationsKeepsOnlySupportedSpainResults() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("https://api.openweathermap.org", "test-key", null, null),
                restTemplate,
                new SimulationProviderTelemetry(meterRegistry));

        server.expect(requestTo("https://api.openweathermap.org/geo/1.0/direct?q=mendoza&limit=25&appid=test-key"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"name\":\"Mendoza\",\"state\":\"Mendoza\",\"country\":\"AR\",\"lat\":-32.8895,\"lon\":-68.8458},{\"name\":\"Sevilla\",\"state\":\"Andalucia\",\"country\":\"ES\",\"lat\":37.3891,\"lon\":-5.9845}]",
                        MediaType.APPLICATION_JSON));

        var results = adapter.searchLocations("mendoza", 5);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Sevilla, Andalucia");
        assertThat(results.getFirst().country()).isEqualTo("ES");
        assertThat(results.getFirst().latitude()).isEqualTo(37.3891);
        assertThat(meterRegistry.counter("simulation_service_provider_calls_total", "provider", "openweather", "outcome", "success").count())
                .isEqualTo(1.0d);
        server.verify();
    }

    @Test
    @DisplayName("searchLocations returns empty list on provider failure when api key is missing")
    void searchLocationsReturnsEmptyListOnProviderFailureWhenApiKeyIsMissing() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("not-a-valid-url", null, null, null),
                new RestTemplate(),
                new SimulationProviderTelemetry(meterRegistry));

        var results = adapter.searchLocations("sevilla", 5);

        assertThat(results).isEmpty();
        assertThat(meterRegistry.counter("simulation_service_provider_calls_total", "provider", "openweather", "outcome", "fallback").count())
                .isEqualTo(1.0d);
    }

    @Test
    @DisplayName("resolveLocation returns unknown on provider failure when api key is missing")
    void resolveLocationReturnsUnknownOnProviderFailureWhenApiKeyIsMissing() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("not-a-valid-url", null, null, null),
                new RestTemplate(),
                new SimulationProviderTelemetry(meterRegistry));

        var result = adapter.resolveLocation(37.3891, -5.9845);

        assertThat(result.name()).isEqualTo("37.3891, -5.9845");
        assertThat(result.country()).isEqualTo("Unknown");
        assertThat(meterRegistry.counter("simulation_service_provider_calls_total", "provider", "openweather", "outcome", "fallback").count())
                .isEqualTo(1.0d);
    }

    @Test
    @DisplayName("resolveLocation counts null payload as fallback")
    void resolveLocationCountsNullPayloadAsFallback() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("https://api.openweathermap.org", "test-key", null, null),
                restTemplate,
                new SimulationProviderTelemetry(meterRegistry));

        server.expect(requestTo("https://api.openweathermap.org/data/2.5/weather?lat=37.3891&lon=-5.9845&units=metric&appid=test-key"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        var result = adapter.resolveLocation(37.3891, -5.9845);

        assertThat(result.name()).isEqualTo("37.3891, -5.9845");
        assertThat(result.country()).isEqualTo("Unknown");
        assertThat(meterRegistry.counter("simulation_service_provider_calls_total", "provider", "openweather", "outcome", "fallback").count())
                .isEqualTo(1.0d);
        server.verify();
    }
}
