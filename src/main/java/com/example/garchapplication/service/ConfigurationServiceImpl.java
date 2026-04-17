package com.example.garchapplication.service;

import com.example.garchapplication.exception.*;
import com.example.garchapplication.helper.CellStylesBuilder;
import com.example.garchapplication.mapper.ConfigurationMapper;
import com.example.garchapplication.model.dto.api.XlsxFileDTO;
import com.example.garchapplication.model.dto.api.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.api.GarchModelDTO;
import com.example.garchapplication.model.dto.api.ConfigurationListItemDTO;
import com.example.garchapplication.model.dto.api.ConfigurationPageDTO;
import com.example.garchapplication.model.dto.api.PageResponse;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.model.enums.CellStyleNames;
import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.model.enums.RoleType;
import com.example.garchapplication.repository.ConfigurationRepository;
import com.example.garchapplication.security.AuthenticationHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final AuditLogService auditLogService;
    private final GarchModelService garchModelService;
    private final ConfigurationRepository configurationRepository;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository, GarchModelServiceImpl garchModelService, AuditLogService auditLogService, AuthenticationHandler authenticationHandler) {
        this.configurationRepository = configurationRepository;
        this.garchModelService = garchModelService;
        this.auditLogService = auditLogService;
        this.authenticationHandler = authenticationHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigurationListItemDTO> getAllConfigurationsByUser() {
        User user = authenticationHandler.getUserEntity();

        if (user == null) {
            return Collections.emptyList();
        }
        return ConfigurationMapper.toListItemDTOs(configurationRepository.getConfigurationsByUser(user));
    }

    @Override
    public ConfigurationPageDTO getConfigurationPageByUser(int page, int size) {
        User user = authenticationHandler.getUserEntity();

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").descending());

        Page<Configuration> configurationList = configurationRepository.findAll(pageable);

        List<ConfigurationListItemDTO> usersConfigurationList = getAllConfigurationsByUser(user);

        PageResponse<ConfigurationListItemDTO> configurationListItemDTOPageResponse = null;

        if (user.getRole() == RoleType.ADMIN) {
            configurationListItemDTOPageResponse = PageResponse.responseFromPage(configurationList.map(ConfigurationMapper::toListItemDTO));
        }
        return new ConfigurationPageDTO(usersConfigurationList, configurationListItemDTOPageResponse);
    }

    private List<ConfigurationListItemDTO> getAllConfigurationsByUser(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return ConfigurationMapper.toListItemDTOs(configurationRepository.getConfigurationsByUser(user));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfiguration(MultipartFile configurationFile) throws IOException {
        validateXlsxFile(configurationFile);

        try (Workbook workbook = new XSSFWorkbook(configurationFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            String configurationName = sheet.getRow(0).getCell(1).getStringCellValue();

            if (configurationName == null || configurationName.isEmpty()) {
                throw new EmptyNameException(EntityType.CONFIGURATION);
            }

            List<GarchModelCalculationDTO> garchModelCalculationDTOS = garchModelService.extractGarchModelsFromFileSheet(sheet);
            Configuration configuration = saveConfiguration(configurationName);

            for (GarchModelCalculationDTO garchModelCalculationDTO : garchModelCalculationDTOS) {
                garchModelService.saveModel(garchModelCalculationDTO, configuration);
            }
        }
    }

    private void validateXlsxFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new WrongFileStructureException("Soubor je prázdný.");
        }

        String filename = multipartFile.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new WrongFileStructureException("Soubor není typu .xlsx.");
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
        try {
            Configuration configuration = new Configuration();
            configuration.setName(configurationName);
            configuration.setUser(user);
            configuration.setCreated(Instant.now());
            configurationRepository.saveAndFlush(configuration);
            auditLogService.logCreateEvent(EntityType.CONFIGURATION, configuration.getId(), configurationName);
            return configuration;
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateNameException(EntityType.CONFIGURATION, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@authorization.canAccessConfiguration(#configurationId, authentication)")
    public void updateConfigurationName(long configurationId, String newName) {
        try {
            Configuration configuration = configurationRepository.findById(configurationId).orElseThrow(() -> new EntityNotFoundException(configurationId, EntityType.CONFIGURATION));
            configuration.setName(newName);
            configurationRepository.flush();
            auditLogService.logUpdateEvent(EntityType.CONFIGURATION, configuration.getId(), newName);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateNameException(EntityType.CONFIGURATION, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@authorization.canAccessConfiguration(#configurationId, authentication)")
    public void deleteConfiguration(long configurationId) {
        String name = configurationRepository.findNameById(configurationId).orElseThrow(() -> new EntityNotFoundException(configurationId, EntityType.CONFIGURATION));

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
    @PreAuthorize("@authorization.canAccessConfiguration(#configurationId, authentication)")
    public XlsxFileDTO exportConfiguration(Long configurationId) {
        Configuration configuration = configurationRepository.findById(configurationId).orElseThrow(() -> new EntityNotFoundException(configurationId, EntityType.CONFIGURATION));
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
            for (GarchModelDTO garchModelDTO : garchModelDTOList) {
                index = garchModelService.addGarchModelToSheet(garchModelDTO, sheet, index, styles);
            }

            sheet.autoSizeColumn(0);
            workbook.write(out);
            return new XlsxFileDTO(out.toByteArray(), configuration.getName());

        } catch (IOException ex) {
            throw new ExportFailedException(EntityType.CONFIGURATION, ex);
        }
    }

    @Override
    public Resource downloadSampleConfiguration() {
        Resource resource = new ClassPathResource("downloads/Configuration-sample.xlsx");

        if (!resource.exists()) {
            throw new SampleNotFoundException(EntityType.CONFIGURATION);
        }

        return resource;
    }
}
