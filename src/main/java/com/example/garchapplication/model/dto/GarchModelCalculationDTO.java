package com.example.garchapplication.model.dto;

import java.util.List;

/**
 * DTO representing a transient GARCH model definition.
 *
 * <p>
 * This object is used when a GARCH model is provided manually
 * (e.g., via user input or Excel import) and is not yet persisted.
 * It contains only the parameters required for volatility calculation
 * and does not include a database identifier.
 *
 * <p>
 * If persistence is required, this DTO must first be converted
 * into a GarchModel entity.
 *
 * @param name name of GARCH model
 * @param startVariance first variance from which the calculation starts
 * @param constantVariance the constant variance
 * @param lastVariances list of variances weight
 * @param lastShocks list of shocks weight
 */
public record GarchModelCalculationDTO(String name, double startVariance, double constantVariance,
                                       List<Double> lastVariances, List<Double> lastShocks) {}
