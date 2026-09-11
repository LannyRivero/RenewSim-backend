package com.renewsim.backend.shared.bootstrap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;
import com.renewsim.backend.scenario_service.infrastructure.persistence.entity.ScenarioEntity;
import com.renewsim.backend.scenario_service.infrastructure.persistence.repository.JpaScenarioRepository;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.repository.JpaTechnologyRepository;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infrastructure.persistence.repo.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@Profile("local | docker | stage | showcase")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "demo.bootstrap", name = "enabled", havingValue = "true")
public class DemoBootstrapDataRunner implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "demo.admin";
    private static final String USER_USERNAME = "demo.user";
    private static final String ADMIN_EMAIL = "demo.admin@renewsim.local";
    private static final String USER_EMAIL = "demo.user@renewsim.local";

    private final RoleJpaRepository roleRepository;
    private final UserJpaRepository userRepository;
    private final JpaTechnologyRepository technologyRepository;
    private final JpaScenarioRepository scenarioRepository;
    private final JpaSimulationRepository simulationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${demo.bootstrap.password:DemoPass123!}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting demo bootstrap data load for stage/showcase environment");

        RoleEntity adminRole = ensureRole(RoleName.ADMIN, "Demo administrator role");
        RoleEntity userRole = ensureRole(RoleName.USER, "Demo user role");

        UserEntity admin = ensureUser(
                ADMIN_USERNAME,
                ADMIN_EMAIL,
                "RenewSim Demo Admin",
                Set.of(adminRole, userRole));
        UserEntity user = ensureUser(
                USER_USERNAME,
                USER_EMAIL,
                "RenewSim Demo User",
                Set.of(userRole));

        TechnologyEntity solar = ensureTechnology(
                "Solar residencial de demostración",
                TechnologyEntity.EnergyType.SOLAR,
                "Sistema fotovoltaico residencial para demos de autoconsumo solar.",
                "1200.00",
                "95.00",
                25,
                "82.00",
                "22.00",
                "1.00",
                "50.00",
                "0.7000",
                "15.00");
        ScenarioEntity solarScenario = ensureScenario(
                "Demo solar residencial - Sevilla",
                "Escenario residencial solar para alimentar dashboard, historial y detalle.",
                solar.getId(),
                "5.00",
                "12000.00",
                "EUR",
                "0.1800",
                "6000.00",
                new ClimateData(5.5, 3.2, 22.0));
        ensureSimulation(admin.getEmail(), solarScenario, List.of(solar.getId()), simulationSeed(
                "Solar residencial viable - Sevilla",
                "Sevilla, España",
                "España",
                "ES",
                37.3891,
                -5.9845,
                "solar",
                5.0,
                6000.0,
                12000.0,
                7800.0,
                1180.0,
                8400.0,
                13.4,
                "recommended",
                "Viable",
                "La producción anual cubre una parte relevante del consumo y el ahorro compensa la inversión.",
                "El recurso solar es alto, el tamaño de la instalación está bien ajustado al consumo y el retorno financiero es positivo."));
        ensureSimulation(admin.getEmail(), solarScenario, List.of(solar.getId()), simulationSeed(
                "Solar viable con reservas - Valencia",
                "Valencia, España",
                "España",
                "ES",
                39.4699,
                -0.3763,
                "solar",
                7.5,
                9200.0,
                16500.0,
                11600.0,
                1580.0,
                12600.0,
                14.8,
                "viable_with_reservations",
                "Viable con reservas",
                "El proyecto tiene buen potencial, pero depende de ajustar consumo diurno o incorporar almacenamiento.",
                "La generación es alta, pero parte del beneficio se pierde si el consumo no coincide con las horas solares."));
        ensureSimulation(admin.getEmail(), solarScenario, List.of(solar.getId()), simulationSeed(
                "Solar no viable - Bilbao",
                "Bilbao, España",
                "España",
                "ES",
                43.2630,
                -2.9350,
                "solar",
                4.0,
                12500.0,
                18000.0,
                3200.0,
                290.0,
                -8200.0,
                -1.7,
                "not_recommended",
                "No viable",
                "La inversión no se recupera con la producción estimada y el ahorro anual es demasiado bajo.",
                "El coste inicial es alto frente a la generación esperada, dejando un VAN negativo y una rentabilidad insuficiente."));
        ensureSimulation(user.getEmail(), solarScenario, List.of(solar.getId()), simulationSeed(
                "Solar con reservas - Málaga",
                "Málaga, España",
                "España",
                "ES",
                36.7213,
                -4.4214,
                "solar",
                3.5,
                4800.0,
                8500.0,
                5200.0,
                690.0,
                2800.0,
                9.1,
                "viable_with_reservations",
                "Viable con reservas",
                "El caso puede funcionar, pero conviene revisar almacenamiento, hábitos de consumo y coste final de instalación.",
                "El retorno existe, aunque el margen es moderado y depende de mejorar el autoconsumo."));
        ensureSimulation(user.getEmail(), solarScenario, List.of(solar.getId()), simulationSeed(
                "Solar no viable - Granada",
                "Granada, España",
                "España",
                "ES",
                37.1773,
                -3.5986,
                "solar",
                4.2,
                5400.0,
                9800.0,
                6500.0,
                860.0,
                4100.0,
                10.6,
                "not_recommended",
                "No viable",
                "Con el coste actual, el ahorro no justifica la inversión; habría que reducir CAPEX o replantear el tamaño.",
                "La instalación genera energía, pero el retorno financiero queda por debajo del umbral aceptable."));

        log.info("Demo bootstrap data load completed");
    }

    private RoleEntity ensureRole(RoleName roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setName(roleName);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private UserEntity ensureUser(String username, String email, String fullName, Set<RoleEntity> roles) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .orElseGet(UserEntity::new);

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(demoPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setActivatedAt(Instant.now());
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private TechnologyEntity ensureTechnology(
            String name,
            TechnologyEntity.EnergyType energyType,
            String description,
            String unitCost,
            String maintenanceCost,
            int lifespanYears,
            String efficiency,
            String capacityFactor,
            String minCapacityKw,
            String maxCapacityKw,
            String co2ReductionFactor,
            String environmentalImpact) {
        return technologyRepository.findByName(name).orElseGet(() -> technologyRepository.save(
                TechnologyEntity.builder()
                        .name(name)
                        .energyType(energyType)
                        .description(description)
                        .unitCost(new BigDecimal(unitCost))
                        .maintenanceCost(new BigDecimal(maintenanceCost))
                        .lifespanYears(lifespanYears)
                        .efficiency(new BigDecimal(efficiency))
                        .capacityFactor(new BigDecimal(capacityFactor))
                        .minCapacityKw(new BigDecimal(minCapacityKw))
                        .maxCapacityKw(new BigDecimal(maxCapacityKw))
                        .co2ReductionFactor(new BigDecimal(co2ReductionFactor))
                        .environmentalImpact(new BigDecimal(environmentalImpact))
                        .isActive(true)
                        .build()));
    }

    private ScenarioEntity ensureScenario(
            String name,
            String description,
            Long technologyId,
            String defaultCapacityKw,
            String defaultInvestmentAmount,
            String defaultInvestmentCurrency,
            String defaultTariff,
            String defaultConsumption,
            ClimateData climateProfile) {
        return scenarioRepository.findAll().stream()
                .filter(scenario -> name.equalsIgnoreCase(scenario.getName()))
                .findFirst()
                .orElseGet(() -> scenarioRepository.save(ScenarioEntity.builder()
                        .name(name)
                        .description(description)
                        .technologyId(technologyId)
                        .defaultCapacityKw(new BigDecimal(defaultCapacityKw))
                        .defaultInvestmentAmount(new BigDecimal(defaultInvestmentAmount))
                        .defaultInvestmentCurrency(defaultInvestmentCurrency)
                        .defaultTariff(new BigDecimal(defaultTariff))
                        .defaultConsumption(new BigDecimal(defaultConsumption))
                        .climateProfile(climateProfile)
                        .isActive(true)
                        .build()));
    }

    private void ensureSimulation(
            String createdBy,
            ScenarioEntity scenario,
            List<Long> technologyIds,
            SimulationSeed seed) {
        boolean activeSimulationExists = simulationRepository.findByCreatedByOrderByCreatedAtDesc(createdBy).stream()
                .anyMatch(simulation -> seed.name().equalsIgnoreCase(simulation.getName())
                        && !"DELETED".equalsIgnoreCase(simulation.getStatus()));
        if (activeSimulationExists) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SimulationEntity entity = new SimulationEntity();
        entity.setName(seed.name());
        entity.setLocation(seed.locationLabel());
        entity.setEnergyType(seed.energyType());
        entity.setLocationLat(seed.latitude());
        entity.setLocationLng(seed.longitude());
        entity.setProjectSize(seed.installedCapacityKw());
        entity.setCapacityKw(seed.installedCapacityKw());
        entity.setBudget(seed.capexTotal());
        entity.setInitialInvestment(seed.capexTotal());
        entity.setTotalCost(seed.capexTotal());
        entity.setEstimatedEnergy(seed.annualGenerationKwh());
        entity.setClimateData(null);
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(now.minusDays(technologyIds.size() + 1L));
        entity.setUpdatedAt(now.minusDays(technologyIds.size()));
        entity.setStatus("COMPLETED");
        entity.setAnnualSavings(seed.annualSavings());
        entity.setNpv(seed.npv());
        entity.setIrrPct(seed.irrPct());
        entity.setRecommendation(seed.recommendation());
        entity.setScenarioId(scenario.getId());
        entity.setInputSnapshot(inputSnapshot(seed));
        entity.setResultSnapshot(resultSnapshot(seed));
        entity.setTechnologyIds(new ArrayList<>(technologyIds));

        simulationRepository.save(entity);
    }

    private SimulationSeed simulationSeed(
            String name,
            String locationLabel,
            String country,
            String countryCode,
            double latitude,
            double longitude,
            String energyType,
            double installedCapacityKw,
            double annualConsumptionKwh,
            double capexTotal,
            double annualGenerationKwh,
            double annualSavings,
            double npv,
            double irrPct,
            String recommendation,
            String headline,
            String summary,
            String mainReason) {
        return new SimulationSeed(
                name,
                locationLabel,
                country,
                countryCode,
                latitude,
                longitude,
                energyType,
                installedCapacityKw,
                annualConsumptionKwh,
                capexTotal,
                annualGenerationKwh,
                annualSavings,
                npv,
                irrPct,
                recommendation,
                headline,
                summary,
                mainReason);
    }

    private String inputSnapshot(SimulationSeed seed) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("locationCountry", seed.country());
        snapshot.put("locationCountryCode", seed.countryCode());
        snapshot.put("performanceRatio", 0.81);
        snapshot.put("degradationRateAnnualPct", 0.5);
        snapshot.put("availabilityPct", 99.0);
        snapshot.put("lossesInverter", 2.0);
        snapshot.put("lossesTemperature", 6.0);
        snapshot.put("lossesWiring", 1.0);
        snapshot.put("lossesSoiling", 3.0);
        snapshot.put("lossesOther", 1.0);
        snapshot.put("annualConsumptionKwh", seed.annualConsumptionKwh());
        snapshot.put("monthlyConsumptionKwh", monthly(seed.annualConsumptionKwh()));
        snapshot.put("currency", "EUR");
        snapshot.put("opexAnnual", 0.0);
        snapshot.put("electricityPurchasePricePerKwh", 0.18);
        snapshot.put("exportPricePerKwh", 0.07);
        snapshot.put("discountRatePct", 8.0);
        snapshot.put("projectLifetimeYears", 20);
        return writeJson(snapshot);
    }

    private String resultSnapshot(SimulationSeed seed) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("status", "COMPLETED");
        root.put("createdAt", LocalDateTime.now().minusDays(2).toString());
        root.put("updatedAt", LocalDateTime.now().minusDays(1).toString());
        root.put("modelVersion", "demo-bootstrap-v1");
        root.put("technology", seed.energyType());
        root.put("location", Map.of(
                "label", seed.locationLabel(),
                "name", seed.locationLabel(),
                "adminRegion", "Demo",
                "country", seed.country(),
                "countryCode", seed.countryCode(),
                "lat", seed.latitude(),
                "lon", seed.longitude(),
                "timezone", "Europe/Madrid"));
        root.put("summary", Map.of(
                "recommendation", seed.recommendation(),
                "headline", seed.headline(),
                "summary", seed.summary(),
                "reasons", List.of(Map.of(
                        "area", "decisión",
                        "severity", "info",
                        "message", seed.mainReason()))));
        root.put("input", Map.of(
                "name", seed.name(),
                "technology", seed.energyType(),
                "location", Map.of(
                        "label", seed.locationLabel(),
                        "lat", seed.latitude(),
                        "lon", seed.longitude(),
                        "country", seed.country(),
                        "countryCode", seed.countryCode()),
                "system", Map.of(
                        "installedCapacityKw", seed.installedCapacityKw(),
                        "performanceRatio", 0.81,
                        "degradationRateAnnualPct", 0.5,
                        "availabilityPct", 99.0,
                        "lossesPct", Map.of(
                                "inverter", 2.0,
                                "temperature", 6.0,
                                "wiring", 1.0,
                                "soiling", 3.0,
                                "other", 1.0)),
                "demand", Map.of(
                        "annualConsumptionKwh", seed.annualConsumptionKwh(),
                        "monthlyConsumptionKwh", monthly(seed.annualConsumptionKwh())),
                "economics", Map.of(
                        "currency", "EUR",
                        "capexTotal", seed.capexTotal(),
                        "opexAnnual", 0.0,
                        "electricityPurchasePricePerKwh", 0.18,
                        "exportPricePerKwh", 0.07,
                        "discountRatePct", 8.0,
                        "projectLifetimeYears", 20)));
        root.put("technical", Map.of(
                "annualGenerationKwh", seed.annualGenerationKwh(),
                "monthlyGenerationKwh", monthly(seed.annualGenerationKwh()),
                "specificYieldKwhPerKwp", round(seed.annualGenerationKwh() / seed.installedCapacityKw()),
                "performanceRatio", 0.81,
                "capacityFactorPct", 22.0,
                "selfConsumptionRatePct", 72.0,
                "coverageRatePct", round(Math.min(100.0, seed.annualGenerationKwh() / seed.annualConsumptionKwh() * 100.0)),
                "resource", Map.of(
                        "source", "demo-bootstrap",
                        "period", "typical-year",
                        "monthlyIrradianceKwhM2", monthly(1800.0),
                        "monthlyTemperatureC", List.of(10, 11, 13, 16, 20, 24, 28, 27, 23, 18, 13, 10)),
                "lossesPct", Map.of(
                        "inverter", 2.0,
                        "temperature", 6.0,
                        "wiring", 1.0,
                        "soiling", 3.0,
                        "other", 1.0,
                        "total", 13.0),
                "balanceByMonth", monthlyBalance(seed)));
        root.put("financial", Map.of(
                "currency", "EUR",
                "annualSavings", seed.annualSavings(),
                "annualExportRevenue", round(seed.annualSavings() * 0.18),
                "netAnnualBenefit", seed.annualSavings() * 1.18,
                "paybackYears", seed.capexTotal() / Math.max(seed.annualSavings(), 1.0),
                "discountedPaybackYears", seed.capexTotal() / Math.max(seed.annualSavings() * 0.85, 1.0),
                "npv", seed.npv(),
                "irrPct", seed.irrPct(),
                "lcoePerKwh", round(seed.capexTotal() / (seed.annualGenerationKwh() * 20.0)),
                "yearlyCashFlows", yearlyCashFlows(seed)));
        root.put("assumptions", Map.of(
                "discountRatePct", 8.0,
                "projectLifetimeYears", 20,
                "degradationRateAnnualPct", 0.5,
                "electricityPurchasePricePerKwh", 0.18,
                "exportPricePerKwh", 0.07,
                "climateSource", "demo-bootstrap",
                "climatePeriod", "typical-year"));
        root.put("warnings", List.of());
        return writeJson(root);
    }

    private List<Map<String, Object>> monthlyBalance(SimulationSeed seed) {
        List<Double> generation = monthly(seed.annualGenerationKwh());
        List<Double> consumption = monthly(seed.annualConsumptionKwh());
        List<Map<String, Object>> balance = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            double selfConsumed = Math.min(generation.get(i), consumption.get(i));
            balance.add(Map.of(
                    "month", String.valueOf(i + 1),
                    "generationKwh", generation.get(i),
                    "consumptionKwh", consumption.get(i),
                    "selfConsumedKwh", round(selfConsumed),
                    "exportedKwh", round(Math.max(0.0, generation.get(i) - selfConsumed)),
                    "importedKwh", round(Math.max(0.0, consumption.get(i) - selfConsumed))));
        }
        return balance;
    }

    private List<Map<String, Object>> yearlyCashFlows(SimulationSeed seed) {
        List<Map<String, Object>> cashFlows = new ArrayList<>();
        double cumulative = -seed.capexTotal();
        for (int year = 1; year <= 5; year++) {
            double netCashFlow = round(seed.annualSavings() * Math.pow(0.995, year - 1));
            cumulative += netCashFlow;
            cashFlows.add(Map.of(
                    "year", year,
                    "savings", netCashFlow,
                    "exportRevenue", round(seed.annualSavings() * 0.18),
                    "opex", 0.0,
                    "replacementCost", 0.0,
                    "netCashFlow", netCashFlow,
                    "discountedCashFlow", round(netCashFlow / Math.pow(1.08, year)),
                    "cumulativeCashFlow", round(cumulative)));
        }
        return cashFlows;
    }

    private List<Double> monthly(double annualValue) {
        double monthly = Math.round((annualValue / 12.0) * 100.0) / 100.0;
        return List.of(
                monthly, monthly, monthly, monthly, monthly, monthly,
                monthly, monthly, monthly, monthly, monthly, monthly);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to write demo bootstrap JSON", ex);
        }
    }

    private record SimulationSeed(
            String name,
            String locationLabel,
            String country,
            String countryCode,
            double latitude,
            double longitude,
            String energyType,
            double installedCapacityKw,
            double annualConsumptionKwh,
            double capexTotal,
            double annualGenerationKwh,
            double annualSavings,
            double npv,
            double irrPct,
            String recommendation,
            String headline,
            String summary,
            String mainReason) {
    }
}
