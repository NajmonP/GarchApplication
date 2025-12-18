package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.ChartOfTimeSeriesDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;

public final class TimeSeriesChartMapper {
    public static ChartOfTimeSeriesDTO toChart(TimeSeriesDTO ts) {
        var points = ts.timeSeries().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> new ChartOfTimeSeriesDTO.PointDTO(e.getKey(), e.getValue()))
                .toList();
        return new ChartOfTimeSeriesDTO(ts.name(), points);
    }
}
