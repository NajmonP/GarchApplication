package com.example.garchapplication.service;

import com.example.garchapplication.model.TimeSeries;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface TimeSeriesService {
    void addTimeSeries(MultipartFile timeSeriesFile) throws IOException;

    List<TimeSeries> getTimeSeriesByUser();
}
