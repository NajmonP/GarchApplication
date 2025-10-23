package com.example.garchapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface GarchService {
    void calculate(double startVariance, double constantVariance, double lastVariance, double lastShock, MultipartFile timeSeriesFile);
}
