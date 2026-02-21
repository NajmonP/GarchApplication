package com.example.garchapplication.model.dto;

import com.example.garchapplication.model.enums.CalculationStatus;

import java.sql.Date;

public record CalculationDetailDTO(long id, TimeSeriesDetailDTO input, TimeSeriesDetailDTO output, String user, int forecast, CalculationStatus status, Date runAt) {
}
