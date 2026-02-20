package com.example.garchapplication.model.dto;

public record TimeSeriesDetailDTO(ChartOfTimeSeriesDTO chartOfTimeSeriesDTO, int observations, double mean, double skewness, double kurtosis, double min, double max) {
}
