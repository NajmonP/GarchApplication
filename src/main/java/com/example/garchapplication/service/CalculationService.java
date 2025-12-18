package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidConstantVarianceException;
import com.example.garchapplication.exception.InvalidLastValueException;
import com.example.garchapplication.exception.MaxThresholdExceededException;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.entity.Calculation;
import com.example.garchapplication.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.garchapplication.Processes.CalculationProcess;
import com.example.garchapplication.exception.MissingTimeSeriesException;

import java.io.IOException;
import java.util.List;

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
     * @param timeSeriesId   an optional ID of an existing time series (may be null)
     * @throws IOException                      if reading the uploaded time series file fails
     * @throws InvalidConstantVarianceException if the constant variance is below the minimum allowed value
     * @throws InvalidLastValueException        if any of the previous values are non-positive
     * @throws MaxThresholdExceededException    if the total sum exceeds SUM_MAXIMUM_THRESHOLD
     */
    TimeSeriesDTO calculate(GarchModelDTO garchModelDTO, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;

    /**
     * Starting point of calculation based on selected GARCH model.
     *
     * @param modelId        ID of selected GARCH model
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional ID of an existing time series (may be null)
     * @throws IOException if reading the uploaded time series file fails
     */
    TimeSeriesDTO calculateFromSelectedModel(Long modelId, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;

    /**
     * Starts calculation process by creating new instance of {@link CalculationProcess}
     *
     * @param garchModelDTO  GARCH model object sent from calculate methods
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional ID of an existing time series (may be null)
     * @return DTO of time series representing the result of the calculation
     * @throws MissingTimeSeriesException if timeSeriesFile and timeSeriesId are both null
     * @throws IOException                if reading the uploaded time series file fails
     */
    TimeSeriesDTO startCalculationBasedOnInput(GarchModelDTO garchModelDTO, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;

    /**
     * Saves result of the calculation for logged user into database.
     *
     * @param timeSeriesDTO time series representing result of the calculation
     * @param garchModelDTO GARCH model used in calculation
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId an optional ID of an existing time series (may be null)
     * @param user object or logged user
     */
    @Transactional(rollbackFor = Exception.class)
    void saveCalculation(TimeSeriesDTO timeSeriesDTO, GarchModelDTO garchModelDTO, MultipartFile timeSeriesFile, Long timeSeriesId, User user);

    List<Calculation> getAllCalculationsByUser();
}
