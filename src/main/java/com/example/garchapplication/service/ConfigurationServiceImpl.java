package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.repository.ConfigurationRepository;
import com.example.garchapplication.security.UserDetailsImpl;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final UserService userService;
    private final GarchModelService garchModelService;
    private final ConfigurationRepository configurationRepository;


    @Autowired
    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository, UserService userService, GarchModelServiceImpl garchModelService) {
        this.configurationRepository = configurationRepository;
        this.userService = userService;
        this.garchModelService = garchModelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Configuration> getAllConfigurationsByUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return new ArrayList<>();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long userId = ((UserDetailsImpl) userDetails).getId();
        User user = userService.getUserById(userId);

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
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = ((UserDetailsImpl) userDetails).getId();
        User user = userService.getUserById(userId);

        Configuration configuration = new Configuration();
        configuration.setName(configurationName);
        configuration.setUser(user);
        configuration.setCreated(new Date(System.currentTimeMillis()));
        configurationRepository.save(configuration);

        return configuration;
    }
}
