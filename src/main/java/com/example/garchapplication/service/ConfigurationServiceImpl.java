package com.example.garchapplication.service;

import com.example.garchapplication.helper.CellStylesBuilder;
import com.example.garchapplication.model.dto.XlsxFileDTO;
import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.model.enums.CellStyleNames;
import com.example.garchapplication.model.enums.EntityType;
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

    private final AuditLogService auditLogService;
    private final GarchModelService garchModelService;
    private final ConfigurationRepository configurationRepository;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository, GarchModelServiceImpl garchModelService, GarchModelRepository garchModelRepository, AuditLogService auditLogService, AuthenticationHandler authenticationHandler) {
        this.configurationRepository = configurationRepository;
        this.garchModelService = garchModelService;
        this.auditLogService = auditLogService;
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
        auditLogService.logCreateEvent(EntityType.CONFIGURATION, configuration.getId(), configurationName);
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
        auditLogService.logUpdateEvent(EntityType.CONFIGURATION, configuration.getId(), newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfiguration(long configurationId) {
        String name = configurationRepository.findNameById(configurationId).orElseThrow(() -> new RuntimeException("Configuration not found"));

        List<GarchModel> garchModelList = garchModelService.findAllGarchModelsByConfigurationId(configurationId);
        for (GarchModel garchModel : garchModelList) {
            garchModelService.deleteGarchModel(garchModel.getId());
        }
        configurationRepository.deleteById(configurationId);
        auditLogService.logDeleteEvent(EntityType.CONFIGURATION, configurationId, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XlsxFileDTO exportConfiguration(Long configurationId) {
        Configuration configuration = configurationRepository.findById(configurationId).orElseThrow(() -> new RuntimeException("Configuration not found"));
        List<GarchModelDTO> garchModelDTOList = garchModelService.extractGarchModelDTOsByConfigurationId(configurationId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Configuration");
            Map<String, CellStyle> styles = CellStylesBuilder.getCellStylesForConfiguration(workbook);

            Row header = sheet.createRow(0);
            Cell cell = header.createCell(0);
            cell.setCellValue("Název konfigurace:");
            cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
            cell = header.createCell(1);
            cell.setCellValue(configuration.getName());
            cell.setCellStyle(styles.get(CellStyleNames.HEADER.toString()));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 8));
            int index = 1;
            for(GarchModelDTO garchModelDTO : garchModelDTOList) {
                index = garchModelService.addGarchModelToSheet(garchModelDTO, sheet, index, styles);
            }

            sheet.autoSizeColumn(0);
            workbook.write(out);
            return new XlsxFileDTO(out.toByteArray(), configuration.getName());

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }
}
