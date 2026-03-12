package com.example.garchapplication.model.dto.api;

import java.util.List;

public record TimeSeriesPageDTO(
        List<TimeSeriesListItemDTO> myTimeSeries,
        PageResponse<TimeSeriesListItemDTO> publicTimeSeries
) {}
