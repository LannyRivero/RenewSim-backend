package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.simulation_service.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PvgisSolarResourceAdapter implements PvgisSolarResourcePort {

    private final PvgisProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public PvgisSolarResourceProfile fetchProfile(double latitude, double longitude, double systemLossPct) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            JsonNode pvcalc = objectMapper
                    .readTree(restTemplate.getForObject(pvcalcUrl(latitude, longitude, systemLossPct), String.class));
            JsonNode tmy = objectMapper.readTree(restTemplate.getForObject(tmyUrl(latitude, longitude), String.class));

            List<Double> monthlyGeneration = readMonthlyValues(pvcalc.path("outputs").path("monthly").path("fixed"),
                    "E_m");
            List<Double> monthlyIrradiance = readMonthlyValues(pvcalc.path("outputs").path("monthly").path("fixed"),
                    "H(i)_m");
            List<Double> monthlyTemperature = readMonthlyTemperatures(tmy.path("outputs").path("tmy_hourly"));

            if (monthlyGeneration.size() != 12 || monthlyIrradiance.size() != 12 || monthlyTemperature.size() != 12) {
                throw new BadRequestException("PVGIS response is incomplete for monthly solar resource data");
            }

            JsonNode meteoData = pvcalc.path("inputs").path("meteo_data");
            String climatePeriod = meteoData.path("year_min").asText() + "-" + meteoData.path("year_max").asText();

            return new PvgisSolarResourceProfile(monthlyGeneration, monthlyIrradiance, monthlyTemperature,
                    climatePeriod, "PVGIS");
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to fetch PVGIS solar resource data", ex);
        }
    }

    private String pvcalcUrl(double latitude, double longitude, double systemLossPct) {
        return UriComponentsBuilder.fromUriString(properties.url() + "/api/v5_2/PVcalc")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("peakpower", 1)
                .queryParam("loss", systemLossPct)
                .queryParam("optimalangles", 1)
                .queryParam("outputformat", "json")
                .toUriString();
    }

    private String tmyUrl(double latitude, double longitude) {
        return UriComponentsBuilder.fromUriString(properties.url() + "/api/v5_2/tmy")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("outputformat", "json")
                .toUriString();
    }

    private List<Double> readMonthlyValues(JsonNode monthlyNodes, String fieldName) {
        List<Double> values = new ArrayList<>();
        for (JsonNode monthlyNode : monthlyNodes) {
            values.add(monthlyNode.path(fieldName).asDouble());
        }
        return values;
    }

    private List<Double> readMonthlyTemperatures(JsonNode hourlyNodes) {
        double[] sums = new double[12];
        int[] counts = new int[12];
        for (JsonNode hourlyNode : hourlyNodes) {
            String timestamp = hourlyNode.path("time(UTC)").asText();
            if (timestamp.length() < 6) {
                continue;
            }
            int monthIndex = Integer.parseInt(timestamp.substring(4, 6)) - 1;
            if (monthIndex < 0 || monthIndex > 11) {
                continue;
            }
            sums[monthIndex] += hourlyNode.path("T2m").asDouble();
            counts[monthIndex]++;
        }

        List<Double> temperatures = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            temperatures.add(counts[i] == 0 ? 0.0 : sums[i] / counts[i]);
        }
        return temperatures;
    }
}
