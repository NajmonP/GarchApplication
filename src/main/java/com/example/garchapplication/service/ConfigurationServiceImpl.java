package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.ConfigurationFileDTO;
import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.model.enums.CellStyleNames;
import com.example.garchapplication.repository.ConfigurationRepository;
import com.example.garchapplication.repository.GarchModelRepository;
import com.example.garchapplication.security.AuthenticationHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.*;

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
                garchModelService.saveModel(garchModelCalculationDTO, configuration);
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigurationName(long configurationId, String newName) {
        Configuration configuration = configurationRepository.findById(configurationId).orElseThrow(() -> new RuntimeException("Configuration not found"));
        configuration.setName(newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfiguration(long configurationId) {
        List<GarchModel> garchModelList = findAllByConfigurationId(configurationId);
        for (GarchModel garchModel : garchModelList) {
            garchModelService.deleteGarchModel(garchModel.getId());
        }
        configurationRepository.deleteById(configurationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationFileDTO exportConfiguration(Long configurationId) {
        Configuration configuration = configurationRepository.findById(configurationId).orElseThrow(() -> new RuntimeException("Configuration not found"));
        List<GarchModelDTO> garchModelDTOList = extractGarchModelDTOsByConfigurationId(configurationId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Configuration");
            Map<String, CellStyle> styles = getCellStyles(workbook);

            Row header = sheet.createRow(0);
            Cell cell = header.createCell(0);
            cell.setCellValue("Název konfigurace:");
            cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
            cell = header.createCell(1);
            cell.setCellValue(configuration.getName());
            cell.setCellStyle(styles.get(CellStyleNames.CONFIGURATION_NAME.toString()));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 8));
            int index = 1;
            for(GarchModelDTO garchModelDTO : garchModelDTOList) {
                index = garchModelService.addGarchModelToSheet(garchModelDTO, sheet, index, styles);
            }

            sheet.autoSizeColumn(0);
            workbook.write(out);
            return new ConfigurationFileDTO(out.toByteArray(), configuration.getName());

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    private Map<String, CellStyle> getCellStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        CellStyle configurationName = workbook.createCellStyle();
        configurationName.setAlignment(HorizontalAlignment.CENTER);
        configurationName.setVerticalAlignment(VerticalAlignment.CENTER);
        configurationName.setFillForegroundColor(IndexedColors.ORANGE.index);
        configurationName.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.put(CellStyleNames.CONFIGURATION_NAME.toString(), configurationName);

        CellStyle modelName = workbook.createCellStyle();
        modelName.setAlignment(HorizontalAlignment.CENTER);
        modelName.setVerticalAlignment(VerticalAlignment.CENTER);
        modelName.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
        modelName.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.put(CellStyleNames.MODEL_NAME.toString(), modelName);

        CellStyle parameterName = workbook.createCellStyle();
        parameterName.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index);
        parameterName.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styles.put(CellStyleNames.PARAMETER_NAME.toString(), parameterName);
        return styles;
    }
}
