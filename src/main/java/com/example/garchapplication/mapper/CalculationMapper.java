package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.api.CalculationDetailDTO;
import com.example.garchapplication.model.dto.api.TimeSeriesDetailDTO;
import com.example.garchapplication.model.dto.api.CalculationListItemDTO;
import com.example.garchapplication.model.entity.*;

import java.util.List;

public final class CalculationMapper {
    public static CalculationListItemDTO toListItemDTO(Calculation calculation) {
        return new CalculationListItemDTO(
                calculation.getId(),
                calculation.getStatus(),
                calculation.getRunAt(),
                calculation.getUser().getUsername(),
                calculation.getInputTimeSeries() != null ? calculation.getInputTimeSeries().getId() : null,
                calculation.getInputTimeSeries() != null ? calculation.getInputTimeSeries().getName() : null,
                calculation.getResultTimeSeries() != null ? calculation.getResultTimeSeries().getId() : null,
                calculation.getResultTimeSeries() != null ? calculation.getResultTimeSeries().getName() : null
        );
    }

    public static List<CalculationListItemDTO> toListItemDTOs(List<Calculation> calculationList) {
        return calculationList.stream()
                .map(CalculationMapper::toListItemDTO)
                .toList();
    }

    public static CalculationDetailDTO toDetailDTO(Calculation calculation,
                                                   TimeSeriesDetailDTO timeSeriesDetailDTOInput,
                                                   TimeSeriesDetailDTO timeSeriesDetailDTOResult,
                                                   List<RunVarianceWeight> varianceWeights,
                                                   List<RunShockWeight> shockWeights) {
        List<Double> lastVariances = varianceWeights.stream()
                .map(RunVarianceWeight::getValue)
                .toList();

        List<Double> lastShocks = shockWeights.stream()
                .map(RunShockWeight::getValue)
                .toList();

        return new CalculationDetailDTO(
                calculation.getId(),
                timeSeriesDetailDTOInput,
                timeSeriesDetailDTOResult,
                calculation.getForecast(),
                calculation.getStatus(),
                calculation.getStartVariance(),
                calculation.getConstantVariance(),
                lastVariances,
                lastShocks
        );
    }
}
