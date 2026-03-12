package com.example.garchapplication.model.dto.api;

import java.util.List;

public record CalculationPageDTO(
        List<CalculationListItemDTO> myCalculations,
        PageResponse<CalculationListItemDTO> allCalculations
) {
}
