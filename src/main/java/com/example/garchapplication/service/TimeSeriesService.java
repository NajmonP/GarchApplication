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

    TimeSeriesDTO getTimeSeriesFromFile(MultipartFile timeSeriesFile, GarchModelDTO garchModelDTO) throws IOException;

    TimeSeriesDTO getTimeSeriesFromDatabase(Long timeSeriesId, GarchModelDTO garchModelDTO);
}
