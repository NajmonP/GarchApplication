package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidConstatVarianceException;
import com.example.garchapplication.exception.InvalidLastValueException;
import com.example.garchapplication.exception.MaxThresholdExceededException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CalculationServiceImpl implements CalculationService {

    private static final double SUM_MAXIMUM_THRESHOLD = 1.0;
    private static final double MINIMUM_VALUE = 0.0;

    @Override
    public void calculate(double startVariance, double constantVariance, List<Double> lastVariance, List<Double> lastShock, MultipartFile timeSeriesFile) {
        if(constantVariance < MINIMUM_VALUE) {
            throw new InvalidConstatVarianceException();
        }
        double sum = 0.0;
        for(double variance: lastVariance) {
            if(variance <= MINIMUM_VALUE) {
                throw new InvalidLastValueException();
            }
            sum += variance;
        }
        for(double shock: lastShock) {
            if(shock <= MINIMUM_VALUE) {
                throw new InvalidLastValueException();
            }
            sum += shock;
        }
        if(sum > SUM_MAXIMUM_THRESHOLD) {
            throw new MaxThresholdExceededException(sum);
        }
    }

    @Override
    public void calculateFromSelectedModel(Long modelId, MultipartFile timeSeriesFile) {

    }
}
