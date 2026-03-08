package com.example.garchapplication.model.dto.api;

import com.example.garchapplication.model.dto.ChartOfTimeSeriesDTO;

public record TimeSeriesDetailDTO(long id, ChartOfTimeSeriesDTO chartOfTimeSeriesDTO, int observations, double mean, double skewness, double kurtosis, double min, double max) {
}
