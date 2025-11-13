package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.GarchModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service interface responsible for GARCH model management.
 */
@Service
public interface GarchModelService {
    List<GarchModel> findAllByConfigurationId(Long configurationId);

    /**
     * Loads GARCH model and its parameters from database based on given ID.
     * <br>
     * Data is loaded from {@code garch_model} entity and its paramteres from {@code model_variance_weight}
     * and {@code model_shock_weight} entities.
     *
     * @param modelId ID of selected GARCH model
     * @return DTO of selected GARCH model
     */
    GarchModelDTO extractGarchModelDTO(Long modelId);
}
