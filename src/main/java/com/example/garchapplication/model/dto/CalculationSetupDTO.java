package com.example.garchapplication.model.dto;

import java.util.Map;

public record CalculationSetupDTO(Map<Long, Double> timeSeries, GarchModelCalculationDTO garchModelCalculationDTO, int forecast) {
}
