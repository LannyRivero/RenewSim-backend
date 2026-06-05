package com.renewsim.backend.scenario_service.infrastructure.persistence.entity;

import com.renewsim.backend.scenario_service.infrastructure.persistence.converter.ClimateDataJsonConverter;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "technology_id", nullable = false)
    private Long technologyId;

    @Column(name = "default_capacity_kw", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultCapacityKw;

    @Column(name = "default_investment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultInvestmentAmount;

    @Column(name = "default_investment_currency", nullable = false, length = 10)
    private String defaultInvestmentCurrency;

    @Column(name = "default_tariff", nullable = false, precision = 10, scale = 4)
    private BigDecimal defaultTariff;

    @Column(name = "default_consumption", nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultConsumption;

    @Convert(converter = ClimateDataJsonConverter.class)
    @Column(name = "climate_profile", columnDefinition = "JSON", nullable = false)
    private ClimateData climateProfile;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
