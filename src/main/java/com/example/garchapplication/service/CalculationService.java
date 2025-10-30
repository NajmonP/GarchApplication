package com.example.garchapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface CalculationService {
    void calculate(double startVariance, double constantVariance, List<Double> lastVariance, List<Double> lastShock, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;

    void calculateFromSelectedModel(Long modelId, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;
}
