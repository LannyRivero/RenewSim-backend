package com.renewsim.backend.scenario_service.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter
public class ClimateDataJsonConverter implements AttributeConverter<ClimateData, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Override
    public String convertToDatabaseColumn(ClimateData attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Could not serialize ClimateData", ex);
        }
    }

    @Override
    public ClimateData convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(dbData, ClimateData.class);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not deserialize ClimateData", ex);
        }
    }
}
