package com.example.garchapplication.model.dto;

import java.util.List;

public record ChartOfTimeSeriesDTO(String name, List<PointDTO> points) {
    public record PointDTO(long x, double y) {
    }
}
