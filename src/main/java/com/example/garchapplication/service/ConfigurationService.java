package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.AuditInfoDTO;
import com.example.garchapplication.model.dto.api.XlsxFileDTO;
import com.example.garchapplication.model.dto.api.ConfigurationListItemDTO;
import com.example.garchapplication.model.dto.api.ConfigurationPageDTO;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service interface responsible for configuration management.
 */
@Service
public interface ConfigurationService {
    /**
     * Process of adding new user configuration and its GARCH models to database.
     *
     * @param configurationFile uploaded configuration file
     * @throws IOException  if reading the uploaded configuration file fails
     */
    @Transactional(rollbackFor = Exception.class)
    void addConfiguration(MultipartFile configurationFile) throws IOException;

    /**
     * Loads all configurations associated with the current user from database.
     *
     * @return all configurations associated with the current user.
     */
    List<ConfigurationListItemDTO> getAllConfigurationsByUser();

    ConfigurationPageDTO getConfigurationPageByUser(int page, int size);

    /**
     * Updates configuration name.
     *
     * @param configurationId id of configuration that is going to be updated
     * @param newName new name of configuration
     */
    @Transactional(rollbackFor = Exception.class)
    void updateConfigurationName(long configurationId, String newName);

    /**
     * Deletes configuration na its corresponding models.
     *
     * @param configurationId id of configuration that is going to be deleted
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteConfiguration(long configurationId);


    /**
     * Loads all configurations data from database and writes them into xlsx file .
     *
     * @param configurationId id of configuration that is going to be exported
     * @return DTO containing configuration name and corresponding data
     */
    XlsxFileDTO exportConfiguration(Long configurationId);

    Resource downloadSampleConfiguration();

    List<AuditInfoDTO> findAllAuditInfoByUserId(Long userId);
}
