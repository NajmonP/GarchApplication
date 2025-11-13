package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidConstantVarianceException;
import com.example.garchapplication.exception.InvalidLastValueException;
import com.example.garchapplication.exception.MaxThresholdExceededException;
import com.example.garchapplication.model.dto.GarchModelDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.garchapplication.Processes.CalculationProcess;
import com.example.garchapplication.exception.MissingTimeSeriesException;

import java.io.IOException;

/**
 * Service interface responsible for performing GARCH model calculations.
 * <br>
 * Provides methods for calculation based either on user input or selected GARCH model.
 */
@Service
public interface CalculationService {
    /**
     * Starting point of calculation based on user input.
     * <br>
     * Validates user input.
     *
     * @param garchModelDTO  GARCH model object made of user input
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional uploaded time series file (may be null)
     * @throws IOException                      if reading the uploaded time series file fails
     * @throws InvalidConstantVarianceException if the constant variance is below the minimum allowed value
     * @throws InvalidLastValueException        if any of the previous values are non-positive
     * @throws MaxThresholdExceededException    if the total sum exceeds SUM_MAXIMUM_THRESHOLD
     */
    void calculate(GarchModelDTO garchModelDTO, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;

    /**
     * Starting point of calculation based on selected GARCH model.
     *
     * @param modelId        ID of selected GARCH model
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional uploaded time series file (may be null)
     * @throws IOException if reading the uploaded time series file fails
     */
    void calculateFromSelectedModel(Long modelId, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;

    /**
     * Starts calculation process by creating new instance of {@link CalculationProcess}
     *
     * @param garchModelDTO  GARCH model object sent from calculate methods
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional uploaded time series file (may be null)
     * @throws MissingTimeSeriesException if timeSeriesFile and timeSeriesId are both null
     * @throws IOException                if reading the uploaded time series file fails
     */
    void startCalculationBasedOnInput(GarchModelDTO garchModelDTO, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;
}
