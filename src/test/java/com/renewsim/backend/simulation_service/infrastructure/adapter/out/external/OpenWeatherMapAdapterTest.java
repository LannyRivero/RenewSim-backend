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
    @DisplayName("searchLocations maps upstream results into resolved locations")
    void searchLocationsMapsResolvedLocations() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        OpenWeatherMapAdapter adapter = new OpenWeatherMapAdapter(
                new WeatherServiceProperties("https://api.openweathermap.org", "test-key", null, null),
                restTemplate);

        server.expect(requestTo("https://api.openweathermap.org/geo/1.0/direct?q=mendoza&limit=5&appid=test-key"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"name\":\"Mendoza\",\"state\":\"Mendoza\",\"country\":\"AR\",\"lat\":-32.8895,\"lon\":-68.8458}]",
                        MediaType.APPLICATION_JSON));

        var results = adapter.searchLocations("mendoza", 5);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Mendoza, Mendoza");
        assertThat(results.getFirst().country()).isEqualTo("AR");
        assertThat(results.getFirst().latitude()).isEqualTo(-32.8895);
        server.verify();
    }
}
