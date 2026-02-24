package com.example.garchapplication.service;

import com.example.garchapplication.Processes.CalculationProcess;
import com.example.garchapplication.model.dto.*;
import com.example.garchapplication.exception.InvalidConstantVarianceException;
import com.example.garchapplication.exception.InvalidLastValueException;
import com.example.garchapplication.exception.MaxThresholdExceededException;
import com.example.garchapplication.exception.MissingTimeSeriesException;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.model.enums.CalculationStatus;
import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.repository.CalculationRepository;
import com.example.garchapplication.repository.RunShockWeightRepository;
import com.example.garchapplication.repository.RunVarianceWeightRepository;
import com.example.garchapplication.security.AuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link CalculationService}.
 * Handles validation, loading of time series data, and initialization of the GARCH calculation process.
 */
@Service
public class CalculationServiceImpl implements CalculationService {

    private final AuditLogService auditLogService;
    private final TimeSeriesService timeSeriesService;
    private final GarchModelService garchModelService;
    private final AuthenticationHandler authenticationHandler;
    private final RunVarianceWeightRepository runVarianceWeightRepository;
    private final RunShockWeightRepository runShockWeightRepository;
    private final CalculationRepository calculationRepository;

    private static final double SUM_MAXIMUM_THRESHOLD = 1.0;
    private static final double MINIMUM_VALUE = 0.0;
    private static final String RESULT_NAME = "result";

    @Autowired
    public CalculationServiceImpl(AuditLogService auditLogService, TimeSeriesService timeSeriesService, GarchModelService garchModelService, AuthenticationHandler authenticationHandler, RunVarianceWeightRepository runVarianceWeightRepository, RunShockWeightRepository runShockWeightRepository, CalculationRepository calculationRepository) {
        this.auditLogService = auditLogService;
        this.timeSeriesService = timeSeriesService;
        this.garchModelService = garchModelService;
        this.authenticationHandler = authenticationHandler;
        this.runVarianceWeightRepository = runVarianceWeightRepository;
        this.runShockWeightRepository = runShockWeightRepository;
        this.calculationRepository = calculationRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimeSeriesDTO calculate(GarchModelCalculationDTO garchModelCalculationDTO, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        validateInput(garchModelCalculationDTO.constantVariance(), garchModelCalculationDTO.lastVariances(), garchModelCalculationDTO.lastShocks());
        TimeSeriesDTO result = startCalculationBasedOnInput(garchModelCalculationDTO, forecast, timeSeriesFile, timeSeriesId);
        User user = authenticationHandler.getUserEntity();
        if (user != null) {
            saveCalculation(result, garchModelCalculationDTO, forecast, timeSeriesFile, timeSeriesId, user);
        }
        return result;
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
    @Transactional(rollbackFor = Exception.class)
    public TimeSeriesDTO calculateFromSelectedModel(Long modelId, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        GarchModelCalculationDTO garchModelCalculationDTO = garchModelService.extractGarchModelCalculationDTO(modelId);
        return calculate(garchModelCalculationDTO, forecast, timeSeriesFile, timeSeriesId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeSeriesDTO startCalculationBasedOnInput(GarchModelCalculationDTO garchModelCalculationDTO, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId) throws IOException {
        TimeSeriesDTO loadedTimeSeries;

        if (timeSeriesFile != null && !timeSeriesFile.isEmpty()) {
            loadedTimeSeries = timeSeriesService.getTimeSeriesFromFile(timeSeriesFile);
        } else if (timeSeriesId != null) {
            loadedTimeSeries = timeSeriesService.getTimeSeriesDTOFromDatabase(timeSeriesId);
        } else {
            throw new MissingTimeSeriesException();
        }

        CalculationSetupDTO calculationSetupDTO = new CalculationSetupDTO(loadedTimeSeries.timeSeries(), garchModelCalculationDTO, forecast);
        CalculationProcess calculationProcess = new CalculationProcess(calculationSetupDTO);
        Map<Long, Double> predictedVolatility = calculationProcess.startCalculation();

        return new TimeSeriesDTO(RESULT_NAME, predictedVolatility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCalculation(TimeSeriesDTO timeSeriesDTO, GarchModelCalculationDTO garchModelCalculationDTO, int forecast, MultipartFile timeSeriesFile, Long timeSeriesId, User user) {
        Calculation calculation = new Calculation();
        calculation.setRunAt(Instant.now());
        calculation.setUser(user);
        calculation.setStartVariance(garchModelCalculationDTO.startVariance());
        calculation.setConstantVariance(garchModelCalculationDTO.constantVariance());

        if (timeSeriesFile != null && !timeSeriesFile.isEmpty()) {
            calculation.setStatus(CalculationStatus.MISSING_INPUT_SERIES);
            calculation.setInputTimeSeries(null);
        } else {
            calculation.setStatus(CalculationStatus.OK);
            calculation.setInputTimeSeries(timeSeriesService.getTimeSeriesFromDatabase(timeSeriesId));
        }

        calculationRepository.save(calculation);

        String newName = RESULT_NAME + "_" + calculation.getId();
        TimeSeriesDTO renamed = new TimeSeriesDTO(newName, timeSeriesDTO.timeSeries());

        TimeSeries resultTimeSeries = timeSeriesService.addTimeSeriesFromDTO(renamed);
        calculation.setResultTimeSeries(resultTimeSeries);
        calculation.setForecast(forecast);

        // update
        calculationRepository.save(calculation);

        for (int i = 0; i < garchModelCalculationDTO.lastVariances().size(); i++) {
            saveRunVarianceWeight(calculation, garchModelCalculationDTO.lastVariances().get(i), i);
        }

        for (int i = 0; i < garchModelCalculationDTO.lastShocks().size(); i++) {
            saveRunShockWeight(calculation, garchModelCalculationDTO.lastShocks().get(i), i);
        }
        auditLogService.logCreateEvent(EntityType.CALCULATION, calculation.getId(), null);
    }

    private void saveRunVarianceWeight(Calculation calculation, double value, int index) {
        RunVarianceWeight runVarianceWeight = new RunVarianceWeight();
        runVarianceWeight.setCalculation(calculation);
        runVarianceWeight.setOrderNo(index + 1);
        runVarianceWeight.setValue(value);
        runVarianceWeightRepository.save(runVarianceWeight);
    }

    private void saveRunShockWeight(Calculation calculation, double value, int index) {
        RunShockWeight runShockWeight = new RunShockWeight();
        runShockWeight.setCalculation(calculation);
        runShockWeight.setOrderNo(index + 1);
        runShockWeight.setValue(value);
        runShockWeightRepository.save(runShockWeight);
    }

    @Override
    public List<Calculation> getAllCalculationsByUser() {
        User user = authenticationHandler.getUserEntity();

        if (user == null) {
            return Collections.emptyList();
        }
        return calculationRepository.getCalculationsByUser(user);
    }

    @Override
    public Calculation getCalculationById(long calculationId) {
        return calculationRepository.getCalculationById(calculationId);
    }

    @Override
    public CalculationDetailDTO getCalculationDetails(long calculationId) {
        Calculation calculation = getCalculationById(calculationId);
        Optional<TimeSeries> optionalTimeSeriesInput = Optional.ofNullable(calculation.getInputTimeSeries());
        Optional<TimeSeries> optionalTimeSeriesResult = Optional.ofNullable(calculation.getResultTimeSeries());
        TimeSeriesDetailDTO timeSeriesDetailDTOInput = null;
        TimeSeriesDetailDTO timeSeriesDetailDTOResult = null;
        if (optionalTimeSeriesInput.isPresent()) {
            timeSeriesDetailDTOInput = timeSeriesService.getTimeSeriesDetails(optionalTimeSeriesInput.get().getId());
        }
        if (optionalTimeSeriesResult.isPresent()) {
            timeSeriesDetailDTOResult = timeSeriesService.getTimeSeriesDetails(calculation.getResultTimeSeries().getId());
        }

        return new CalculationDetailDTO(calculationId, timeSeriesDetailDTOInput, timeSeriesDetailDTOResult, calculation.getUser().getUsername(), calculation.getForecast(), calculation.getStatus(), calculation.getRunAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCalculation(long calculationId) {
        calculationRepository.deleteById(calculationId);
        auditLogService.logDeleteEvent(EntityType.CALCULATION, calculationId, null);
    }
}
