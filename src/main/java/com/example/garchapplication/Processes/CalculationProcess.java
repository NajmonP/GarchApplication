package com.example.garchapplication.Processes;

import com.example.garchapplication.model.dto.CalculationSetupDTO;

import java.util.*;

/**
 * Performs calculation based on attribute parameters given in constructor
 */
public class CalculationProcess {

    private final double startVariance;
    private final double constantVariance;
    private final List<Double> lastVariancesList;
    private final List<Double> lastShocksList;
    private final Map<Long, Double> timeSeries;

    public CalculationProcess(CalculationSetupDTO calculationSetupDTO) {
        this.startVariance = calculationSetupDTO.garchModelDTO().startVariance();
        this.constantVariance = calculationSetupDTO.garchModelDTO().constantVariance();
        this.lastVariancesList = calculationSetupDTO.garchModelDTO().lastVariances();
        this.lastShocksList = calculationSetupDTO.garchModelDTO().lastShocks();
        this.timeSeries = calculationSetupDTO.timeSeries();
    }

    /**
     * Starting point of the calculation process.
     *
     * @return result of the calculation which is time series of future variances predictions
     */
    public Map<Long, Double> startCalculation() {
        prepareResidues();
        return predictVolatility();
    }

    /**
     * Calculates residues by subtracting the simple moving average from all time series values.
     */
    private void prepareResidues() {
        double sum = 0.0;

        for (Double value : timeSeries.values()) {
            sum += value;
        }

        double mean = sum / timeSeries.size();

        for (Long key : timeSeries.keySet()) {
            timeSeries.compute(key, (k, value) -> value - mean);
        }
    }

    /**
     * Predicts volatility from given residues.
     * <br>
     * Performs first call of recursive function predictNextVolatility.
     *
     * @return time series of future variances predictions
     */
    private Map<Long, Double> predictVolatility() {
        Map<Long, Double> predictedVolatility = new HashMap<>();
        predictedVolatility.put(1L, startVariance);
        predictNextVolatility(predictedVolatility, 2L);
        return predictedVolatility;
    }

    /**
     * Recursive function calculating that adds new value to future variances predictions map.
     * <br>
     * Recursion is stopped when size of predictedVolatility map reaches the size of time series in class attribute.
     *
     * @param predictedVolatility future variances predictions map
     * @param index tracks the recursion and represents new key to future variances predictions map
     */
    private void predictNextVolatility(Map<Long, Double> predictedVolatility, Long index) {
        double nextVariance = constantVariance;
        long lastVarianceIndex = index - 1;
        long lastShockIndex = index - 1;
        for (Double lastVariance : lastVariancesList) {
            if (lastVarianceIndex < 1) {
                break;
            }
            nextVariance += lastVariance * Math.pow(timeSeries.get(lastVarianceIndex), 2);
            lastVarianceIndex--;
        }
        for (Double lastShock : lastShocksList) {
            if (lastShockIndex < 1) {
                break;
            }
            nextVariance += lastShock * predictedVolatility.get(lastShockIndex);
            lastShockIndex--;
        }

        predictedVolatility.put(index, nextVariance);

        if (timeSeries.containsKey(index + 1)) {
            predictNextVolatility(predictedVolatility, index + 1);
        }
    }
}
