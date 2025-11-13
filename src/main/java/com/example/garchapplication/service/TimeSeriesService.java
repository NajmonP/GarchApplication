package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.entity.TimeSeries;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface TimeSeriesService {
    void addTimeSeries(MultipartFile timeSeriesFile) throws IOException;

    List<TimeSeries> getTimeSeriesByUser();

    /**
     * Loads time series data from uploaded time series file.
     *
     * @param timeSeriesFile uploaded time series file from user input
     * @return DTO of uploaded time series
     * @throws IOException if reading the uploaded time series file fails
     */
    TimeSeriesDTO getTimeSeriesFromFile(MultipartFile timeSeriesFile) throws IOException;

    /**
     * Loads time series data from database based on user input.
     *
     * @param timeSeriesId ID of selected time series
     * @return DTO of selected time series
     */
    TimeSeriesDTO getTimeSeriesFromDatabase(Long timeSeriesId);
}
