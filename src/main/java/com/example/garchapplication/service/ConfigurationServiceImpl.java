package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.repository.ConfigurationRepository;
import com.example.garchapplication.repository.GarchModelRepository;
import com.example.garchapplication.repository.ModelShockWeightRepository;
import com.example.garchapplication.repository.ModelVarianceWeightRepository;
import com.example.garchapplication.security.UserDetailsImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ConfigurationRepository configurationRepository;
    private final GarchModelRepository garchModelRepository;
    private final ModelVarianceWeightRepository modelVarianceWeightRepository;
    private final ModelShockWeightRepository modelShockWeightRepository;

    private final int MODEL_ROWS = 5;

    @Autowired
    private ConfigurationServiceImpl(ConfigurationRepository configurationRepository, UserService userService, GarchModelRepository garchModelRepository, ModelVarianceWeightRepository modelVarianceWeightRepository, ModelShockWeightRepository modelShockWeightRepository) {

        this.configurationRepository = configurationRepository;
        this.userService = userService;
        this.garchModelRepository = garchModelRepository;
        this.modelVarianceWeightRepository = modelVarianceWeightRepository;
        this.modelShockWeightRepository = modelShockWeightRepository;
    }

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

    @Override
    public void addConfiguration(MultipartFile configurationFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(configurationFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            String configurationName = sheet.getRow(0).getCell(1).getStringCellValue();
            List<GarchModelDTO> garchModelDTOs = extractGarchModelsFromFileSheet(sheet);
            Configuration configuration = saveConfiguration(configurationName);

            for (GarchModelDTO garchModelDTO : garchModelDTOs) {
                GarchModel garchModel = saveModel(garchModelDTO, configuration);
                for (int i = 0; i < garchModelDTO.lastVariances().size(); i++) {
                    saveModelVarianceWeight(garchModelDTO, garchModel, i);
                }
                for (int i = 0; i < garchModelDTO.lastShocks().size(); i++) {
                    saveModelShockWeight(garchModelDTO, garchModel, i);
                }
            }
        }
    }

    private List<GarchModelDTO> extractGarchModelsFromFileSheet(Sheet sheet) {
        List<GarchModelDTO> garchModelDTOs = new ArrayList<>();

        for (int i = 1; i < sheet.getLastRowNum(); i += MODEL_ROWS) {
            String modelName = "";
            double startVariance = 0;
            double constantVariance = 0;
            List<Double> lastVariances = new ArrayList<>();
            List<Double> lastShocks = new ArrayList<>();
            for (int j = i; j < i + MODEL_ROWS; j++) {
                Row row = sheet.getRow(j);
                switch (j % MODEL_ROWS) {
                    case 0 -> {
                        for (int currentCellNum = 1; currentCellNum < row.getLastCellNum(); currentCellNum++) {
                            lastShocks.add(row.getCell(currentCellNum).getNumericCellValue());
                        }
                    }
                    case 1 -> modelName = row.getCell(1).getStringCellValue();
                    case 2 -> startVariance = row.getCell(1).getNumericCellValue();
                    case 3 -> constantVariance = row.getCell(1).getNumericCellValue();
                    case 4 -> {
                        for (int currentCellNum = 1; currentCellNum < row.getLastCellNum(); currentCellNum++) {
                            lastVariances.add(row.getCell(currentCellNum).getNumericCellValue());
                        }
                    }
                }
            }
            garchModelDTOs.add(new GarchModelDTO(modelName, startVariance, constantVariance, lastVariances, lastShocks));
        }

        return garchModelDTOs;
    }

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

    private GarchModel saveModel(GarchModelDTO garchModelDTO, Configuration configuration) {
        GarchModel garchModel = new GarchModel();
        garchModel.setConfiguration(configuration);
        garchModel.setName(garchModelDTO.name());
        garchModel.setStartVariance(garchModelDTO.startVariance());
        garchModel.setConstantVariance(garchModelDTO.constantVariance());
        garchModelRepository.save(garchModel);

        return garchModel;
    }

    private void saveModelVarianceWeight(GarchModelDTO garchModelDTO, GarchModel garchModel, int index) {
        ModelVarianceWeight modelVarianceWeight = new ModelVarianceWeight();
        modelVarianceWeight.setGarchModel(garchModel);
        modelVarianceWeight.setOrder(index + 1);
        modelVarianceWeight.setValue(garchModelDTO.lastVariances().get(index));
        modelVarianceWeightRepository.save(modelVarianceWeight);
    }

    private void saveModelShockWeight(GarchModelDTO garchModelDTO, GarchModel garchModel, int index) {
        ModelShockWeight modelShockWeight = new ModelShockWeight();
        modelShockWeight.setGarchModel(garchModel);
        modelShockWeight.setOrder(index + 1);
        modelShockWeight.setValue(garchModelDTO.lastShocks().get(index));
        modelShockWeightRepository.save(modelShockWeight);
    }
}
