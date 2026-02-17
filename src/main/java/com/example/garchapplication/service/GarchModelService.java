package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.model.entity.GarchModel;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service interface responsible for GARCH model management.
 */
@Service
public interface GarchModelService {
    /**
     * Loads GARCH model and its parameters from database based on given ID.
     * <br>
     * Data is loaded from {@code garch_model} entity and its parameters from {@code model_variance_weight}
     * and {@code model_shock_weight} entities.
     *
     * @param modelId ID of selected GARCH model
     * @return CalculationDTO of selected GARCH model
     */
    GarchModelCalculationDTO extractGarchModelCalculationDTO(Long modelId);

    /**
     * Loads GARCH model and its parameters from database based on given ID.
     * <br>
     * Data is loaded from {@code garch_model} entity and its parameters from {@code model_variance_weight}
     * and {@code model_shock_weight} entities.
     *
     * @param modelId ID of selected GARCH model
     * @return DTO of selected GARCH model
     */
    GarchModelDTO extractGarchModelDTO(Long modelId);

    /**
     * Extracts all GARCH models from sheet of provided configuration file.
     *
     * @param sheet sheet to extract GARCH models from
     * @return list of GARCH models
     */
    List<GarchModelCalculationDTO> extractGarchModelsFromFileSheet(Sheet sheet);

    /**
     * Adds new GARCH model of given configuration to database.
     *
     * @param garchModelCalculationDTO GARCH model to be saved into database
     * @param configuration configuration that GARCH model belongs to
     * @return instance of saved GARCH model for purpose of saving related model variance and shock weights
     */
    GarchModel saveModel(GarchModelCalculationDTO garchModelCalculationDTO, Configuration configuration);

    /**
     * Adds variance weight of the given GARCH model to database.
     *
     * @param garchModel GARCH model that variance weight belongs to
     * @param value value of the variance weight
     * @param index order of the variance weight
     */
    void saveModelVarianceWeight(GarchModel garchModel, double value,  int index);

    /**
     * Adds shock weight of the given GARCH model to database.
     *
     * @param garchModel GARCH model that shock weight belongs
     * @param value value of the shock weight
     * @param index order of the shock weight
     */
    void saveModelShockWeight(GarchModel garchModel, double value, int index);
}
