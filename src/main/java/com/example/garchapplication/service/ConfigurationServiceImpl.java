package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.repository.ConfigurationRepository;
import com.example.garchapplication.repository.GarchModelRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final GarchModelService garchModelService;
    private final ConfigurationRepository configurationRepository;
    private final GarchModelRepository garchModelRepository;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository, GarchModelServiceImpl garchModelService, GarchModelRepository garchModelRepository, AuthenticationHandler authenticationHandler) {
        this.configurationRepository = configurationRepository;
        this.garchModelService = garchModelService;
        this.garchModelRepository = garchModelRepository;
        this.authenticationHandler = authenticationHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Configuration> getAllConfigurationsByUser() {
        User user = authenticationHandler.getUserEntity();

        if (user == null) {
            return Collections.emptyList();
        }
        return configurationRepository.getConfigurationsByUser(user);
    }


    @Override
    public List<GarchModel> findAllByConfigurationId(Long configurationId) {
        return garchModelRepository.findAllByConfigurationId(configurationId);
    }

    @Override
    public List<GarchModelDTO> extractGarchModelDTOsByConfigurationId(Long configurationId) {
        List<GarchModel> garchModelList = findAllByConfigurationId(configurationId);
        List<GarchModelDTO> garchModelDTOList = new ArrayList<>();
        garchModelList.forEach(garchModel -> {
            garchModelDTOList.add(garchModelService.extractGarchModelDTO(garchModel.getId()));
        });
        return garchModelDTOList;
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
            List<GarchModelCalculationDTO> garchModelCalculationDTOS = garchModelService.extractGarchModelsFromFileSheet(sheet);
            Configuration configuration = saveConfiguration(configurationName);

            for (GarchModelCalculationDTO garchModelCalculationDTO : garchModelCalculationDTOS) {
                GarchModel garchModel = garchModelService.saveModel(garchModelCalculationDTO, configuration);
                for (int i = 0; i < garchModelCalculationDTO.lastVariances().size(); i++) {
                    garchModelService.saveModelVarianceWeight(garchModel, garchModelCalculationDTO.lastVariances().get(i), i);
                }
                for (int i = 0; i < garchModelCalculationDTO.lastShocks().size(); i++) {
                    garchModelService.saveModelShockWeight(garchModel, garchModelCalculationDTO.lastShocks().get(i), i);
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
