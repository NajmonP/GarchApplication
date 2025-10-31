package com.example.garchapplication.model.dto;

import java.util.Map;

public record TimeSeriesDTO(String name, Map<Long, Double> timeSeries) {
}
