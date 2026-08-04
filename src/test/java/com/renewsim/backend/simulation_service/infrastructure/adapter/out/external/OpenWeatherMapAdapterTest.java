package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("https://api.openweathermap.org", "test-key", null, null),
                restTemplate);

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
        server.verify();
    }

    @Test
    @DisplayName("searchLocations returns empty list on provider failure when api key is missing")
    void searchLocationsReturnsEmptyListOnProviderFailureWhenApiKeyIsMissing() {
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("not-a-valid-url", null, null, null),
                new RestTemplate());

        var results = adapter.searchLocations("sevilla", 5);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("resolveLocation returns unknown on provider failure when api key is missing")
    void resolveLocationReturnsUnknownOnProviderFailureWhenApiKeyIsMissing() {
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("not-a-valid-url", null, null, null),
                new RestTemplate());

        var result = adapter.resolveLocation(37.3891, -5.9845);

        assertThat(result.name()).isEqualTo("37.3891, -5.9845");
        assertThat(result.country()).isEqualTo("Unknown");
    }
}
