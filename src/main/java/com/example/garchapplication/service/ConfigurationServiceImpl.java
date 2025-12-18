package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.repository.ConfigurationRepository;
import com.example.garchapplication.security.AuthenticationHandler;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final GarchModelService garchModelService;
    private final ConfigurationRepository configurationRepository;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository, GarchModelServiceImpl garchModelService, AuthenticationHandler authenticationHandler) {
        this.configurationRepository = configurationRepository;
        this.garchModelService = garchModelService;
        this.authenticationHandler = authenticationHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Configuration> getAllConfigurationsByUser(){
        User user = authenticationHandler.getUserEntity();

        if(user == null){
            return Collections.emptyList();
        }
        return configurationRepository.getConfigurationsByUser(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfiguration(MultipartFile configurationFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(configurationFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            String configurationName = sheet.getRow(0).getCell(1).getStringCellValue();
            List<GarchModelDTO> garchModelDTOs = garchModelService.extractGarchModelsFromFileSheet(sheet);
            Configuration configuration = saveConfiguration(configurationName);

            for (GarchModelDTO garchModelDTO : garchModelDTOs) {
                GarchModel garchModel = garchModelService.saveModel(garchModelDTO, configuration);
                for (int i = 0; i < garchModelDTO.lastVariances().size(); i++) {
                    garchModelService.saveModelVarianceWeight(garchModel, garchModelDTO.lastVariances().get(i), i);
                }
                for (int i = 0; i < garchModelDTO.lastShocks().size(); i++) {
                    garchModelService.saveModelShockWeight(garchModel,garchModelDTO.lastShocks().get(i), i);
                }
            }
        }
    }

    /**
     * Adds new configuration to database.
     *
     * @param configurationName name of the configuration acquired from configuration file
     * @return instance of saved configuration for purpose of saving related GARCH models
     */
    private Configuration saveConfiguration(String configurationName) {
        User user = authenticationHandler.getUserEntity();

        Configuration configuration = new Configuration();
        configuration.setName(configurationName);
        configuration.setUser(user);
        configuration.setCreated(new Date(System.currentTimeMillis()));
        configurationRepository.save(configuration);

        return configuration;
    }
}
