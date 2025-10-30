package com.example.garchapplication.dto;

import java.util.Map;

public record CalculationSetupDTO(Map<Long, Double> timeSeries, GarchModelDTO garchModelDTO) {
}
