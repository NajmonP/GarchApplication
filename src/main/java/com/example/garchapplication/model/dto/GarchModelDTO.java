package com.example.garchapplication.model.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a GARCH model.
 * <p>
 * Each GARCH model is stored in the database as a {@code garch_model} entity,
 * with its corresponding variance and shock weights represented by
 * {@code model_variance_weight} and {@code model_shock_weight} entities.
 * <br>
 * This DTO is used to transfer all relevant GARCH model data together,
 * instead of sending the {@code GarchModel} entity along with separate
 * lists of {@code ModelShockWeight} and {@code ModelVarianceWeight} entities.
 *
 * @param name name of GARCH model
 * @param startVariance first variance from which the calculation starts
 * @param constantVariance the constant variance
 * @param lastVariances list of variances weight
 * @param lastShocks list of shocks weight
 */
public record GarchModelDTO(String name, double startVariance, double constantVariance,
                     List<Double> lastVariances, List<Double> lastShocks) {}
