package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidGarchParamsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.InvalidParameterException;

@Service
public class GarchServiceImpl implements GarchService {

    private static final double SUM_THRESHOLD = 1.0;

    @Override
    public void calculate(double startVariance, double constantVariance, double lastVariance, double lastShock, MultipartFile timeSeriesFile) {
        if(lastVariance + lastShock > SUM_THRESHOLD) {
            throw new InvalidGarchParamsException(lastShock, lastVariance);
        }
    }
}
