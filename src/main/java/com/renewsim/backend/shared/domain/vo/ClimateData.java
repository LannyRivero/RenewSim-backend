package com.renewsim.backend.shared.domain.vo;

public record ClimateData(
    double avgSolarIrradiation,
    double avgWindSpeed,
    double avgTemperature
) {
    // Sin validaciones complejas según spec
    // Valores pueden ser 0 o negativos según dominio climático
}
