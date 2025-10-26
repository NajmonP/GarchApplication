package com.example.garchapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface GarchService {
    void calculate(double startVariance, double constantVariance, List<Double> lastVariance, List<Double> lastShock, MultipartFile timeSeriesFile);

    void calculateFromSelectedModel(Long modelId, MultipartFile timeSeriesFile);
}
