package com.example.garchapplication.mapper;


import com.example.garchapplication.model.dto.api.ConfigurationListItemDTO;
import com.example.garchapplication.model.entity.Configuration;

import java.util.List;

public final class ConfigurationMapper {
    public static ConfigurationListItemDTO toListItemDTO(Configuration configuration) {
        return new ConfigurationListItemDTO(
                configuration.getId(),
                configuration.getName(),
                configuration.getCreated()
        );
    }

    public static List<ConfigurationListItemDTO> toListItemDTOs(List<Configuration> list) {
        return list.stream()
                .map(ConfigurationMapper::toListItemDTO)
                .toList();
    }
}
