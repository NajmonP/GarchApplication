package com.example.garchapplication.model.dto.api;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a persisted GARCH model.
 * <p>
 * This object is used when a GARCH model is loaded from the database
 * and returned to the client. It contains the database identifier
 * together with all parameters required for volatility calculation.
 * <p>
 * Unlike {@link GarchModelCalculationDTO}, this DTO represents
 * an already stored model and therefore always contains a non-null identifier.
 *
 * @param id id of GARCH model
 * @param name name of GARCH model
 * @param startVariance first variance from which the calculation starts
 * @param constantVariance the constant variance
 * @param lastVariances list of variances weight
 * @param lastShocks list of shocks weight
 */
public record GarchModelDTO(long id, String name, double startVariance, double constantVariance,
                            List<Double> lastVariances, List<Double> lastShocks) {}
