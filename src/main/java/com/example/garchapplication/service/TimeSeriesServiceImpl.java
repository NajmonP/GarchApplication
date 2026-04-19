package com.example.garchapplication.service;

import com.example.garchapplication.exception.*;
import com.example.garchapplication.helper.CellStylesBuilder;
import com.example.garchapplication.mapper.TimeSeriesChartMapper;
import com.example.garchapplication.mapper.TimeSeriesMapper;
import com.example.garchapplication.model.dto.AuditInfoDTO;
import com.example.garchapplication.model.dto.api.ChartOfTimeSeriesDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.dto.api.*;
import com.example.garchapplication.model.dto.api.XlsxFileDTO;
import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.model.entity.TimeSeriesValue;
import com.example.garchapplication.model.entity.User;
import com.example.garchapplication.model.enums.CellStyleNames;
import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.repository.CalculationRepository;
import com.example.garchapplication.repository.TimeSeriesRepository;
import com.example.garchapplication.repository.TimeSeriesValueRepository;
import com.example.garchapplication.repository.UserRepository;
import com.example.garchapplication.security.AuthenticationHandler;
import com.example.garchapplication.security.AuthorizationService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class TimeSeriesServiceImpl implements TimeSeriesService {

    private final AuditLogService auditLogService;
    private final TimeSeriesRepository timeSeriesRepository;
    private final CalculationRepository calculationRepository;
    private final TimeSeriesValueRepository timeSeriesValueRepository;
    private final AuthenticationHandler authenticationHandler;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    @Autowired
    public TimeSeriesServiceImpl(AuditLogService auditLogService, TimeSeriesRepository timeSeriesRepository, CalculationRepository calculationRepository, TimeSeriesValueRepository timeSeriesValueRepository, AuthenticationHandler authenticationHandler, UserRepository userRepository, AuthorizationService authorizationService) {
        this.auditLogService = auditLogService;
        this.timeSeriesRepository = timeSeriesRepository;
        this.calculationRepository = calculationRepository;
        this.timeSeriesValueRepository = timeSeriesValueRepository;
        this.authenticationHandler = authenticationHandler;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public List<TimeSeriesListItemDTO> getTimeSeriesByUser() {
        Long userId = authenticationHandler.getUserId();

        if (userId == null) {
            return Collections.emptyList();
        }
        return TimeSeriesMapper.toListItemDTOs(timeSeriesRepository.getTimeSeriesByUserId(userId));
    }

    @Override
    public TimeSeriesPageDTO getTimeSeriesPageByUser(int page, int size) {
        Long userId = authenticationHandler.getUserId();

        List<TimeSeriesListItemDTO> usersTimeSeriesList = getTimeSeriesByUserId(userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").descending());

        Page<TimeSeries> publicTimeSeriesList;

        if (userId != null && authorizationService.isAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            publicTimeSeriesList = timeSeriesRepository.findAll(pageable);
        } else {
            publicTimeSeriesList = timeSeriesRepository.findPublicTimeSeries(pageable);
        }

        PageResponse<TimeSeriesListItemDTO> timeSeriesListItemDTOS = PageResponse.responseFromPage(publicTimeSeriesList.map(TimeSeriesMapper::toListItemDTO));

        return new TimeSeriesPageDTO(usersTimeSeriesList, timeSeriesListItemDTOS);
    }

    private List<TimeSeriesListItemDTO> getTimeSeriesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return TimeSeriesMapper.toListItemDTOs(timeSeriesRepository.getTimeSeriesByUserId(userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTimeSeriesFromFile(MultipartFile timeSeriesFile) throws IOException {
        validateXlsxFile(timeSeriesFile);

        try (Workbook workbook = new XSSFWorkbook(timeSeriesFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            String timeSeriesName = sheet.getRow(0).getCell(1).getStringCellValue();

            TimeSeries timeSeries = saveTimeSeries(timeSeriesName);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                double value = sheet.getRow(i).getCell(0).getNumericCellValue();
                saveTimeSeriesValue(timeSeries, value, i - 1);
            }
        } catch (IllegalStateException | NullPointerException ex) {
            throw new WrongFileStructureException(EntityType.TIME_SERIES, ex);
        }
    }

    private void validateXlsxFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new WrongFileStructureException("Soubor je prázdný");
        }

        String filename = multipartFile.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new WrongFileStructureException("Soubor není typu .xlsx");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimeSeries addTimeSeriesFromDTO(TimeSeriesDTO timeSeriesDTO) {
        TimeSeries timeSeries = saveTimeSeries(timeSeriesDTO.name());

        Map<Long, Double> timeSeriesValues = timeSeriesDTO.timeSeries();

        for (int i = 0; i < timeSeriesValues.size(); i++) {
            saveTimeSeriesValue(timeSeries, timeSeriesValues.get((long) i), i + 1);
        }

        return timeSeries;
    }

    /**
     * Saves time series into database.
     *
     * @param timeSeriesName name of new time series
     * @return saved time series stored in database
     */
    public TimeSeries saveTimeSeries(String timeSeriesName) {
        Optional<User> optionalUser = userRepository.findById(authenticationHandler.getUserId());
        try {
            TimeSeries timeSeries = new TimeSeries();
            timeSeries.setUser(optionalUser.get());
            timeSeries.setName(timeSeriesName);
            timeSeries.setCreated(Instant.now());
            timeSeries.setVisibility("Private");

            timeSeriesRepository.saveAndFlush(timeSeries);
            auditLogService.logCreateEvent(EntityType.TIME_SERIES, timeSeries.getId(), timeSeriesName);
            return timeSeries;
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateNameException(EntityType.TIME_SERIES, ex);
        }
    }

    /**
     * Save time series value into database.
     *
     * @param timeSeries time series of given value
     * @param value      value of next time series observation
     * @param rowNum     index of given value
     */
    public void saveTimeSeriesValue(TimeSeries timeSeries, double value, int rowNum) {
        TimeSeriesValue timeSeriesValue = new TimeSeriesValue();
        timeSeriesValue.setTimeSeries(timeSeries);
        timeSeriesValue.setValue(value);
        timeSeriesValue.setOrderNo(rowNum);
        timeSeriesValueRepository.save(timeSeriesValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeSeriesDTO getTimeSeriesFromFile(MultipartFile timeSeriesFile) throws IOException {
        validateXlsxFile(timeSeriesFile);

        Map<Long, Double> loadedTimeSeries = new HashMap<>();
        String timeSeriesName;
        try (Workbook workbook = new XSSFWorkbook(timeSeriesFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            timeSeriesName = sheet.getRow(0).getCell(1).getStringCellValue();

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                double value = sheet.getRow(i).getCell(0).getNumericCellValue();
                loadedTimeSeries.put((long) (i - 2), value);
            }
        } catch (IllegalStateException | NullPointerException ex) {
            throw new WrongFileStructureException(EntityType.TIME_SERIES, ex);
        }
        return new TimeSeriesDTO(timeSeriesName, loadedTimeSeries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("@authorization.canAccessTimeSeries(#timeSeriesId, authentication)")
    public TimeSeriesDTO getTimeSeriesDTOFromDatabase(Long timeSeriesId) {
        String name = timeSeriesRepository.findById(timeSeriesId).orElseThrow(() -> new EntityNotFoundException(timeSeriesId, EntityType.TIME_SERIES)).getName();

        List<TimeSeriesValue> timeSeriesValueList = timeSeriesValueRepository.findAllByTimeSeriesIdOrderByOrderNo(timeSeriesId);
        Map<Long, Double> timeSeries = new HashMap<>();
        for (int i = 0; i < timeSeriesValueList.size(); i++) {
            timeSeries.put((long) i, timeSeriesValueList.get(i).getValue());
        }
        return new TimeSeriesDTO(name, timeSeries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("@authorization.canAccessTimeSeries(#timeSeriesId, authentication)")
    public TimeSeries getTimeSeriesFromDatabase(Long timeSeriesId) {
        return timeSeriesRepository.findById(timeSeriesId).orElseThrow(() -> new EntityNotFoundException(timeSeriesId, EntityType.TIME_SERIES));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@authorization.canAccessTimeSeries(#timeSeriesId, authentication)")
    public void updateTimeSeries(Long timeSeriesId, UpdateTimeSeriesRequest updateTimeSeriesRequest) {
        String newName = updateTimeSeriesRequest.name();
        String visibility = updateTimeSeriesRequest.visibility();

        if (newName == null || newName.isBlank()) {
            throw new EmptyNameException(EntityType.TIME_SERIES);
        }
        try {
            TimeSeries timeSeries = timeSeriesRepository.findById(timeSeriesId).orElseThrow(() -> new EntityNotFoundException(timeSeriesId, EntityType.TIME_SERIES));
            timeSeries.setName(newName);
            timeSeries.setVisibility(visibility);
            timeSeriesRepository.flush();
            auditLogService.logUpdateEvent(EntityType.TIME_SERIES, timeSeries.getId(), newName);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateNameException(EntityType.TIME_SERIES, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("@authorization.canAccessTimeSeries(#timeSeriesId, authentication)")
    public void deleteTimeSeries(long timeSeriesId) {
        String name = timeSeriesRepository.findNameById(timeSeriesId).orElseThrow(() -> new EntityNotFoundException(timeSeriesId, EntityType.TIME_SERIES));

        calculationRepository.markMissingInput(timeSeriesId);
        calculationRepository.markMissingOutput(timeSeriesId);

        timeSeriesRepository.deleteById(timeSeriesId);

        calculationRepository.markBrokenWhereBothNull();

        auditLogService.logDeleteEvent(EntityType.TIME_SERIES, timeSeriesId, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("@authorization.canAccessTimeSeriesDetails(#timeSeriesId, authentication)")
    public XlsxFileDTO exportTimeSeries(long timeSeriesId) {
        TimeSeriesDTO timeSeriesDTO = getTimeSeriesDTOFromDatabase(timeSeriesId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("TimeSeries");
            Map<String, CellStyle> styles = CellStylesBuilder.getCellStylesForTimeSeries(workbook);

            Row header = sheet.createRow(0);
            Cell cell = header.createCell(0);
            cell.setCellValue("Název konfigurace:");
            cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));
            cell = header.createCell(1);
            cell.setCellValue(timeSeriesDTO.name());
            cell.setCellStyle(styles.get(CellStyleNames.HEADER.toString()));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 8));

            Row row = sheet.createRow(1);
            cell = row.createCell(0);
            cell.setCellValue("Hodnoty: ");
            cell.setCellStyle(styles.get(CellStyleNames.PARAMETER_NAME.toString()));

            exportTimeSeriesValues(sheet, timeSeriesDTO.timeSeries());

            sheet.autoSizeColumn(0);
            workbook.write(out);
            return new XlsxFileDTO(out.toByteArray(), timeSeriesDTO.name());

        } catch (IOException ex) {
            throw new ExportFailedException(EntityType.TIME_SERIES, ex);
        }
    }

    private void exportTimeSeriesValues(Sheet sheet, Map<Long, Double> timeSeriesValues) {
        for (int i = 0; i < timeSeriesValues.size(); i++) {
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(timeSeriesValues.get((long) i));
        }
    }

    @Override
    @PreAuthorize("@authorization.canAccessTimeSeriesDetails(#timeSeriesId, authentication)")
    public TimeSeriesDetailDTO getTimeSeriesDetails(Long timeSeriesId) {
        TimeSeriesDTO timeSeriesDTO = getTimeSeriesDTOFromDatabase(timeSeriesId);
        ChartOfTimeSeriesDTO chartOfTimeSeriesDTO = TimeSeriesChartMapper.toChart(timeSeriesDTO);
        List<Double> values = calculateStatisticalDetails(timeSeriesDTO.timeSeries());
        return new TimeSeriesDetailDTO(timeSeriesId, chartOfTimeSeriesDTO, timeSeriesDTO.timeSeries().size(), values.get(0), values.get(1), values.get(2), values.get(3), values.get(4));
    }

    @Override
    public Resource downloadSampleTimeSeries() {
        Resource resource = new ClassPathResource("downloads/Time-series-sample.xlsx");

        if (!resource.exists()) {
            throw new SampleNotFoundException(EntityType.TIME_SERIES);
        }

        return resource;
    }

    private List<Double> calculateStatisticalDetails(Map<Long, Double> timeSeries) {
        List<Double> values = new ArrayList<>();
        double sum = 0.0;

        for (Double value : timeSeries.values()) {
            sum += value;
        }

        int numberOfValues = timeSeries.size();

        double mean = sum / numberOfValues;

        double standardDeviation = 0;
        for (Double value : timeSeries.values()) {
            standardDeviation += Math.pow(value - mean, 2);
        }
        standardDeviation = standardDeviation / (numberOfValues - 1);
        standardDeviation = Math.sqrt(standardDeviation);

        values.add(mean);

        double skewness = 0;
        for (Double value : timeSeries.values()) {
            skewness += Math.pow(value - mean, 3);
        }
        skewness = skewness / ((numberOfValues - 1) * Math.pow(standardDeviation, 3));
        values.add(skewness);

        double kurtosis = 0;
        for (Double value : timeSeries.values()) {
            kurtosis += Math.pow(value - mean, 4);
        }
        kurtosis = kurtosis / ((numberOfValues - 1) * Math.pow(standardDeviation, 4));
        values.add(kurtosis);

        values.add(Collections.min(timeSeries.values()));
        values.add(Collections.max(timeSeries.values()));
        return values;
    }

    @Override
    public List<AuditInfoDTO> findAllAuditInfoByUserId(Long userId) {
        return timeSeriesRepository.findAllAuditInfoByUserId(userId);
    }
}
