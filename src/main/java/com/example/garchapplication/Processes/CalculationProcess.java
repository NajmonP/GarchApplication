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
        long startIndex = Math.max(lastVariancesList.size(), lastShocksList.size());
        for (int i = 0; i < startIndex; i++) {
            predictedVolatility.put((long) i, startVariance);
        }
        predictNextVolatility(predictedVolatility, startIndex);
        return predictedVolatility;
    }

    /**
     * Recursive function calculating that adds new value to future variances predictions map.
     * <br>
     * Recursion is stopped when size of predictedVolatility map reaches the size of time series in class attribute.
     *
     * @param predictedVolatility future variances predictions map
     * @param index               tracks the recursion and represents new key to future variances predictions map
     */
    private void predictNextVolatility(Map<Long, Double> predictedVolatility, Long index) {
        double nextVariance = constantVariance;
        long currentIndex = index;
        for (Double lastVariance : lastVariancesList) {
            nextVariance += lastVariance * Math.pow(timeSeries.get(currentIndex - 1), 2);
            currentIndex--;
        }
        currentIndex = index;
        for (Double lastShock : lastShocksList) {
            nextVariance += lastShock * predictedVolatility.get(currentIndex - 1);
            currentIndex--;
        }

        predictedVolatility.put(index, nextVariance);

        if (timeSeries.containsKey(index + 1)) {
            predictNextVolatility(predictedVolatility, index + 1);
        }
    }
}
