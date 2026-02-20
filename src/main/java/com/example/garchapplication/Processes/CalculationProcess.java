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
    private int forecast;
    private final Map<Long, Double> timeSeries;

    public CalculationProcess(CalculationSetupDTO calculationSetupDTO) {
        this.startVariance = calculationSetupDTO.garchModelCalculationDTO().startVariance();
        this.constantVariance = calculationSetupDTO.garchModelCalculationDTO().constantVariance();
        this.lastVariancesList = calculationSetupDTO.garchModelCalculationDTO().lastVariances();
        this.lastShocksList = calculationSetupDTO.garchModelCalculationDTO().lastShocks();
        this.forecast = calculationSetupDTO.forecast();
        this.timeSeries = calculationSetupDTO.timeSeries();
    }

    /**
     * Starting point of the calculation process.
     *
     * @return result of the calculation which is time series calculated and forecasted variances
     */
    public Map<Long, Double> startCalculation() {
        prepareResidues();
        return calculateVolatility();
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
     * calculates volatility from given residues.
     * <br>
     * Performs first call of recursive function calculateNextVolatility.
     *
     * @return time series of calculated and forecasted variances
     */
    private Map<Long, Double> calculateVolatility() {
        Map<Long, Double> calculatedVolatility = new HashMap<>();
        long startIndex = Math.max(lastVariancesList.size(), lastShocksList.size());
        for (int i = 0; i < startIndex; i++) {
            calculatedVolatility.put((long) i, startVariance);
        }
        calculateNextVolatility(calculatedVolatility, startIndex);
        return calculatedVolatility;
    }

    /**
     * Recursive function that adds new value to calculated and forecasted variances map.
     * <br>
     * Recursion is stopped when size of calculatedVolatility map reaches the size of time series in class attribute
     * and forecast attribute drops to 0.
     *
     * @param calculatedVolatility calculated variances map
     * @param index                tracks the recursion and represents new key to calculated variances map
     */
    private void calculateNextVolatility(Map<Long, Double> calculatedVolatility, Long index) {
        double nextVariance = constantVariance;
        long currentIndex = index;
        int missingVariances = 0;
        for (Double lastVariance : lastVariancesList) {
            if (timeSeries.size() > currentIndex - 1) {
                nextVariance += lastVariance * Math.pow(timeSeries.get(currentIndex - 1), 2);
            } else {
                missingVariances++;
            }
            currentIndex--;
        }
        currentIndex = index;
        for (Double lastShock : lastShocksList) {
            for (int i = 0; i < missingVariances; i++) {
                nextVariance += lastVariancesList.get(i) * calculatedVolatility.get(currentIndex - 1);
            }
            nextVariance += lastShock * calculatedVolatility.get(currentIndex - 1);
            currentIndex--;
        }

        calculatedVolatility.put(index, nextVariance);

        if (timeSeries.containsKey(index + 1)) {
            calculateNextVolatility(calculatedVolatility, index + 1);
            return;
        }

        if (forecast-- > 0) {
            calculateNextVolatility(calculatedVolatility, index + 1);
        }
    }
}
