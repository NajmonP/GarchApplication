package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.api.CalculationListItemDTO;
import com.example.garchapplication.model.entity.Calculation;

import java.util.List;

public final class CalculationMapper {
    public static CalculationListItemDTO toListItemDTO(Calculation calculation) {
        return new CalculationListItemDTO(
                calculation.getId(),
                calculation.getStatus().name(),
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
}
