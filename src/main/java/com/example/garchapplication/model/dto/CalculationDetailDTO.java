package com.example.garchapplication.model.dto;

import com.example.garchapplication.model.dto.api.TimeSeriesDetailDTO;
import com.example.garchapplication.model.enums.CalculationStatus;

import java.util.List;


public record CalculationDetailDTO(long id,
                                   TimeSeriesDetailDTO input,
                                   TimeSeriesDetailDTO output,
                                   int forecast,
                                   CalculationStatus status,
                                   double startVariance,
                                   double constantVariance,
                                   List<Double> lastVariances,
                                   List<Double> lastShocks) {
}
