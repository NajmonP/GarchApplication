package com.example.garchapplication.model.dto.api;

import java.util.List;

public record ConfigurationPageDTO(
        List<ConfigurationListItemDTO> myConfigurations,
        PageResponse<ConfigurationListItemDTO> allConfigurations
) {
}
