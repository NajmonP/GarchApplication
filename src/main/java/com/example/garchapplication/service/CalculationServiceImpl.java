package com.example.garchapplication.service;

import com.example.garchapplication.Processes.CalculationProcess;
import com.example.garchapplication.model.dto.CalculationSetupDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.exception.InvalidConstatVarianceException;
import com.example.garchapplication.exception.InvalidLastValueException;
import com.example.garchapplication.exception.MaxThresholdExceededException;
import com.example.garchapplication.exception.MissingTimeSeriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculationServiceImpl implements CalculationService {

    private final TimeSeriesService timeSeriesService;
    private final GarchModelService garchModelService;

    private static final double SUM_MAXIMUM_THRESHOLD = 1.0;
    private static final double MINIMUM_VALUE = 0.0;

    @Autowired
    public CalculationServiceImpl(TimeSeriesService timeSeriesService, GarchModelService garchModelService) {
        this.timeSeriesService = timeSeriesService;
        this.garchModelService = garchModelService;
    }

    @Override
    public void calculate(double startVariance, double constantVariance, List<Double> lastVariance, List<Double> lastShock, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        validateInput(constantVariance, lastVariance, lastShock);
        GarchModelDTO garchModelDTO = new GarchModelDTO("manual", startVariance, constantVariance, lastVariance, lastShock);
        startCalculationBasedOnInput(timeSeriesFile, timeSeriesId, garchModelDTO);
    }

    public void validateInput(double constantVariance, List<Double> lastVariance, List<Double> lastShock) {
        if (constantVariance < MINIMUM_VALUE) {
            throw new InvalidConstatVarianceException();
        }
        double sum = 0.0;
        for (double variance : lastVariance) {
            if (variance <= MINIMUM_VALUE) {
                throw new InvalidLastValueException();
            }
            sum += variance;
        }
        for (double shock : lastShock) {
            if (shock <= MINIMUM_VALUE) {
                throw new InvalidLastValueException();
            }
            sum += shock;
        }
        if (sum > SUM_MAXIMUM_THRESHOLD) {
            throw new MaxThresholdExceededException(sum);
        }
    }

    @Override
    public void calculateFromSelectedModel(Long modelId, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        GarchModelDTO garchModelDTO = garchModelService.extractGarchModelDTO(modelId);
        startCalculationBasedOnInput(timeSeriesFile, timeSeriesId, garchModelDTO);
    }

    private void startCalculationBasedOnInput(MultipartFile timeSeriesFile, Long timeSeriesId, GarchModelDTO garchModelDTO) throws IOException {
        Map<Long, Double> predictedVolatility = new HashMap<>();
        TimeSeriesDTO loadedTimeSeries;
        
        if (timeSeriesFile != null && !timeSeriesFile.isEmpty()) {
            loadedTimeSeries = timeSeriesService.getTimeSeriesFromFile(timeSeriesFile, garchModelDTO);
        } else if (timeSeriesId != null) {
            loadedTimeSeries = timeSeriesService.getTimeSeriesFromDatabase(timeSeriesId, garchModelDTO);
        } else {
            throw new MissingTimeSeriesException();
        }
        CalculationSetupDTO calculationSetupDTO = new CalculationSetupDTO(loadedTimeSeries.timeSeries(), garchModelDTO);
        CalculationProcess calculationProcess = new CalculationProcess(calculationSetupDTO);
        predictedVolatility = calculationProcess.startCalculation();
    }
}
