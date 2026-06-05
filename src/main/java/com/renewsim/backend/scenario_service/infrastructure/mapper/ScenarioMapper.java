package com.renewsim.backend.scenario_service.infrastructure.mapper;

import com.renewsim.backend.scenario_service.domain.model.Scenario;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.scenario_service.infrastructure.persistence.entity.ScenarioEntity;
import com.renewsim.backend.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ScenarioMapper {

    public Scenario toDomain(ScenarioEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Scenario(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                new ScenarioTechnologyId(entity.getTechnologyId()),
                new DefaultCapacityKw(entity.getDefaultCapacityKw().doubleValue()),
                new Money(entity.getDefaultInvestmentAmount(), entity.getDefaultInvestmentCurrency()),
                new DefaultTariff(entity.getDefaultTariff().doubleValue()),
                new DefaultConsumption(entity.getDefaultConsumption().doubleValue()),
                entity.getClimateProfile(),
                Boolean.TRUE.equals(entity.getIsActive()));
    }

    public ScenarioEntity toEntity(Scenario domain) {
        if (domain == null) {
            return null;
        }

        return ScenarioEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .technologyId(domain.getTechnologyId())
                .defaultCapacityKw(BigDecimal.valueOf(domain.getDefaultCapacityKw()))
                .defaultInvestmentAmount(domain.getDefaultInvestment().amount())
                .defaultInvestmentCurrency(domain.getDefaultInvestment().currency())
                .defaultTariff(BigDecimal.valueOf(domain.getDefaultTariff()))
                .defaultConsumption(BigDecimal.valueOf(domain.getDefaultConsumption()))
                .climateProfile(domain.getClimateProfile())
                .isActive(domain.isActive())
                .build();
    }
}
