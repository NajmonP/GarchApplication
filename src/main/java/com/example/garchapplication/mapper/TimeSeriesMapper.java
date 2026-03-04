package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.api.TimeSeriesListItemDTO;
import com.example.garchapplication.model.entity.TimeSeries;

import java.util.List;


public final class TimeSeriesMapper {
    public static TimeSeriesListItemDTO toListItemDTO(TimeSeries timeSeries) {
        return new TimeSeriesListItemDTO(
                timeSeries.getId(),
                timeSeries.getName(),
                timeSeries.getCreated(),
                timeSeries.getUser().getUsername(),
                timeSeries.getVisibility()
        );
    }

    public static List<TimeSeriesListItemDTO> toListItemDTOs(List<TimeSeries> list) {
        return list.stream()
                .map(TimeSeriesMapper::toListItemDTO)
                .toList();
    }
}
