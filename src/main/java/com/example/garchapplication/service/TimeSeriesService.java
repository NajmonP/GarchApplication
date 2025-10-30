package com.example.garchapplication.service;

import com.example.garchapplication.dto.GarchModelDTO;
import com.example.garchapplication.model.TimeSeries;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface TimeSeriesService {
    void addTimeSeries(MultipartFile timeSeriesFile) throws IOException;

    List<TimeSeries> getTimeSeriesByUser();

    Map<Long, Double> getTimeSeriesFromFile(MultipartFile timeSeriesFile, GarchModelDTO garchModelDTO) throws IOException;

    Map<Long, Double> getTimeSeriesFromDatabase(Long timeSeriesId, GarchModelDTO garchModelDTO);
}
