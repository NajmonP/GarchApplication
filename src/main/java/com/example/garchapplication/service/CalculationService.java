package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidConstantVarianceException;
import com.example.garchapplication.exception.InvalidLastValueException;
import com.example.garchapplication.exception.MaxThresholdExceededException;
import com.example.garchapplication.model.dto.api.CalculationDetailDTO;
import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.dto.api.CalculationPageDTO;
import com.example.garchapplication.model.entity.Calculation;
import com.example.garchapplication.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
     * @param garchModelCalculationDTO  GARCH model object made of user input
     * @param forecast number of observations that are going to forecasted
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional ID of an existing time series (may be null)
     * @throws IOException                      if reading the uploaded time series file fails
     * @throws InvalidConstantVarianceException if the constant variance is below the minimum allowed value
     * @throws InvalidLastValueException        if any of the previous values are non-positive
     * @throws MaxThresholdExceededException    if the total sum exceeds SUM_MAXIMUM_THRESHOLD
     */
    @Transactional(rollbackFor = Exception.class)
    TimeSeriesDTO calculate(GarchModelCalculationDTO garchModelCalculationDTO, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId, Long calculationId) throws IOException;

    /**
     * Starting point of calculation based on selected GARCH model.
     *
     * @param modelId ID of selected GARCH model
     * @param forecast number of observations that are going to forecasted
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional ID of an existing time series (may be null)
     * @throws IOException if reading the uploaded time series file fails
     */
    @Transactional(rollbackFor = Exception.class)
    TimeSeriesDTO calculateFromSelectedModel(Long modelId, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException;

    @Transactional(rollbackFor = Exception.class)
    void rerunCalculation(Long calculationId, Long timeSeriesId) throws IOException;

    /**
     * Starts calculation process by creating new instance of {@link CalculationProcess}
     *
     * @param garchModelCalculationDTO  GARCH model object sent from calculate methods
     * @param forecast number of observations that are going to forecasted
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional ID of an existing time series (may be null)
     * @return DTO of time series representing the result of the calculation
     * @throws MissingTimeSeriesException if timeSeriesFile and timeSeriesId are both null
     * @throws IOException                if reading the uploaded time series file fails
     */
    TimeSeriesDTO startCalculationBasedOnInput(GarchModelCalculationDTO garchModelCalculationDTO, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId, Long calculationId) throws IOException;

    /**
     * Saves result of the calculation for logged user into database.
     *
     * @param timeSeriesDTO time series representing result of the calculation
     * @param garchModelCalculationDTO GARCH model used in calculation
     * @param forecast number of observations that are going to forecasted
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId an optional ID of an existing time series (may be null)
     * @param user object or logged user
     */
    @Transactional(rollbackFor = Exception.class)
    void saveCalculation(TimeSeriesDTO timeSeriesDTO, GarchModelCalculationDTO garchModelCalculationDTO, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId, User user);

    CalculationPageDTO getCalculationPageByUser(int page, int size);

    Calculation getCalculationById(long calculationId);

    CalculationDetailDTO getCalculationDetails(long calculationId);

    @Transactional(rollbackFor = Exception.class)
    void deleteCalculation(long calculationId);
}
