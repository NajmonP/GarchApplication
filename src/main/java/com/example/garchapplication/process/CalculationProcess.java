package com.example.garchapplication.process;

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
    private final int forecast;
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
        return calculateConditionalVariances();
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
     *
     * @return time series of calculated and forecasted variances
     */
    private Map<Long, Double> calculateConditionalVariances() {
        Map<Long, Double> conditionalVariances = new HashMap<>();
        int startIndex = Math.max(lastVariancesList.size(), lastShocksList.size());
        for (int i = 0; i < startIndex; i++) {
            conditionalVariances.put((long) i, startVariance);
        }

        for (int i = startIndex; i < timeSeries.size() + forecast; i++) {
            double variancesValue = 0;
            double shocksValue = 0;
            for (int j = 0; j < lastVariancesList.size(); j++) {
                long lagIndex = (long) i - (j + 1);
                if (timeSeries.containsKey(lagIndex)) {
                    variancesValue += lastVariancesList.get(j) * Math.pow(timeSeries.get(lagIndex), 2);
                } else {
                    variancesValue += lastVariancesList.get(j) * conditionalVariances.get(lagIndex);
                }
            }
            for (int j = 0; j < lastShocksList.size(); j++) {
                long lagIndex = (long) i - (j + 1);
                shocksValue += lastShocksList.get(j) * conditionalVariances.get(lagIndex);
            }
            double nextVariance = constantVariance + variancesValue + shocksValue;
            conditionalVariances.put((long) i, nextVariance);
        }

        return conditionalVariances;
    }
}
