package com.example.garchapplication.service;

import com.example.garchapplication.Processes.CalculationProcess;
import com.example.garchapplication.model.dto.CalculationSetupDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.exception.InvalidConstantVarianceException;
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

/**
 * Implementation of {@link CalculationService}.
 * Handles validation, loading of time series data, and initialization of the GARCH calculation process.
 */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculate(GarchModelDTO garchModelDTO, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        validateInput(garchModelDTO.constantVariance(), garchModelDTO.lastVariances(), garchModelDTO.lastShocks());
        startCalculationBasedOnInput(garchModelDTO, timeSeriesFile, timeSeriesId);
    }

    /**
     * Validates input parameters before performing the calculation.
     * <br>
     * Ensures all variances and shocks are positive and the sum does not exceed a threshold.
     *
     * @param constantVariance the constant variance
     * @param lastVariance     list of last variances weight
     * @param lastShock        list of last shocks weight
     * @throws InvalidConstantVarianceException if the constant variance is below the minimum allowed value
     * @throws InvalidLastValueException        if any of the previous values are non-positive
     * @throws MaxThresholdExceededException    if the total sum exceeds {@link #SUM_MAXIMUM_THRESHOLD}
     */
    private void validateInput(double constantVariance, List<Double> lastVariance, List<Double> lastShock) {
        if (constantVariance < MINIMUM_VALUE) {
            throw new InvalidConstantVarianceException();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculateFromSelectedModel(Long modelId, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        GarchModelDTO garchModelDTO = garchModelService.extractGarchModelDTO(modelId);
        startCalculationBasedOnInput(garchModelDTO, timeSeriesFile, timeSeriesId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startCalculationBasedOnInput(GarchModelDTO garchModelDTO, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        Map<Long, Double> predictedVolatility = new HashMap<>();
        TimeSeriesDTO loadedTimeSeries;

        if (timeSeriesFile != null && !timeSeriesFile.isEmpty()) {
            loadedTimeSeries = timeSeriesService.getTimeSeriesFromFile(timeSeriesFile);
        } else if (timeSeriesId != null) {
            loadedTimeSeries = timeSeriesService.getTimeSeriesFromDatabase(timeSeriesId);
        } else {
            throw new MissingTimeSeriesException();
        }
        CalculationSetupDTO calculationSetupDTO = new CalculationSetupDTO(loadedTimeSeries.timeSeries(), garchModelDTO);
        CalculationProcess calculationProcess = new CalculationProcess(calculationSetupDTO);
        predictedVolatility = calculationProcess.startCalculation();
    }
}
