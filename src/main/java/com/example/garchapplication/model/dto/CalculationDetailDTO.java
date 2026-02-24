package com.example.garchapplication.model.dto;

import com.example.garchapplication.model.enums.CalculationStatus;

import java.time.Instant;

public record CalculationDetailDTO(long id, TimeSeriesDetailDTO input, TimeSeriesDetailDTO output, String user, int forecast, CalculationStatus status, Instant runAt) {
}
