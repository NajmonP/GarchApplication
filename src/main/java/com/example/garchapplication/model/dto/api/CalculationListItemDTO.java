package com.example.garchapplication.model.dto.api;

import com.example.garchapplication.model.enums.CalculationStatus;

import java.time.Instant;

public record CalculationListItemDTO(
        Long id,
        String status,
        Instant runAt,
        String username,
        Long inputTimeSeriesId,
        String inputTimeSeriesName,
        Long resultTimeSeriesId,
        String resultTimeSeriesName
) {}
