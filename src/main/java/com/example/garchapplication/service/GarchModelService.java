package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.api.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.api.GarchModelDTO;
import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.model.entity.GarchModel;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service interface responsible for GARCH model management.
 */
@Service
public interface GarchModelService {

    List<GarchModel> findAllGarchModelsByConfigurationId(Long configurationId);

    /**
     * Loads GARCH model and its parameters from database based on given ID.
     * <br>
     * Data is loaded from {@code garch_model} entity and its parameters from {@code model_alpha}
     * and {@code model_beta} entities.
     *
     * @param modelId ID of selected GARCH model
     * @return CalculationDTO of selected GARCH model
     */
    GarchModelCalculationDTO extractGarchModelCalculationDTO(Long modelId);

    /**
     * Loads GARCH model and its parameters from database based on given ID.
     * <br>
     * Data is loaded from {@code garch_model} entity and its parameters from {@code model_alpha}
     * and {@code model_beta} entities.
     *
     * @param modelId ID of selected GARCH model
     * @return DTO of selected GARCH model
     */
    GarchModelDTO extractGarchModelDTO(Long modelId);

    /**
     * Loads GARCH models and its parameters from database based on given configuration ID.
     * <br>
     * Data is loaded from {@code garch_model} entity and its parameters from {@code model_alpha}
     * and {@code model_beta} entities.
     *
     * @param configurationId ID of selected configuration
     * @return DTOs of corresponding GARCH models
     */
    List<GarchModelDTO> extractGarchModelDTOsByConfigurationId(Long configurationId);

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
     */
    @Transactional(rollbackFor = Exception.class)
    void saveModel(GarchModelCalculationDTO garchModelCalculationDTO, Configuration configuration);

    /**
     * Updates already stored GARCH model and its corresponding shock and variance weights.
     *
     * @param modelId id of GARCH model that is going to be updated
     * @param garchModelCalculationDTO DTO containing new GARCH model data
     */
    @Transactional(rollbackFor = Exception.class)
    void updateGarchModel(long modelId, GarchModelCalculationDTO garchModelCalculationDTO);

    /**
     * Deletes stored GARCH model and its corresponding shock and variance weights.
     *
     * @param modelId id of GARCH model that is about to be deleted
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteGarchModel(Long modelId);

    /**
     * Adds Garch model to Excel sheet during configuration exporting
     *
     * @param garchModelDTO DTO of GARCH model containing all related data
     * @param sheet sheet to what is going to be data written to
     * @param index index of starting row
     * @param styles map of all cell styles
     * @return index of last row that of a sheet to what data is written to
     */
    int addGarchModelToSheet(GarchModelDTO garchModelDTO, Sheet sheet, int index, Map<String, CellStyle> styles);
}
