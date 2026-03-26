package com.example.garchapplication.model.dto;

import com.example.garchapplication.model.dto.api.GarchModelCalculationDTO;

import java.util.Map;

public record CalculationSetupDTO(Map<Long, Double> timeSeries, GarchModelCalculationDTO garchModelCalculationDTO, int forecast) {
}
