package com.example.garchapplication.service;

import com.example.garchapplication.helper.CellStylesBuilder;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.dto.XlsxFileDTO;
import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.model.entity.TimeSeriesValue;
import com.example.garchapplication.model.entity.User;
import com.example.garchapplication.model.enums.CellStyleNames;
import com.example.garchapplication.repository.CalculationRepository;
import com.example.garchapplication.repository.TimeSeriesRepository;
import com.example.garchapplication.repository.TimeSeriesValueRepository;
import com.example.garchapplication.security.AuthenticationHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.*;

@Service
public class TimeSeriesServiceImpl implements TimeSeriesService {

    private final TimeSeriesRepository timeSeriesRepository;
    private final CalculationRepository calculationRepository;
    private final TimeSeriesValueRepository timeSeriesValueRepository;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    public TimeSeriesServiceImpl(TimeSeriesRepository timeSeriesRepository, CalculationRepository calculationRepository, TimeSeriesValueRepository timeSeriesValueRepository, AuthenticationHandler authenticationHandler) {
        this.timeSeriesRepository = timeSeriesRepository;
        this.calculationRepository = calculationRepository;
        this.timeSeriesValueRepository = timeSeriesValueRepository;
        this.authenticationHandler = authenticationHandler;
    }

    @Override
    public List<TimeSeries> getTimeSeriesByUser() {
        User user = authenticationHandler.getUserEntity();

        if (user == null) {
            return Collections.emptyList();
        }
        return timeSeriesRepository.getTimeSeriesByUser(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTimeSeriesFromFile(MultipartFile timeSeriesFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(timeSeriesFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            String timeSeriesName = sheet.getRow(0).getCell(1).getStringCellValue();

            TimeSeries timeSeries = saveTimeSeries(timeSeriesName);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                double value = sheet.getRow(i).getCell(0).getNumericCellValue();
                saveTimeSeriesValue(timeSeries, value, i - 1);
            }
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
        User user = authenticationHandler.getUserEntity();

        TimeSeries timeSeries = new TimeSeries();
        timeSeries.setUser(user);
        timeSeries.setName(timeSeriesName);
        timeSeries.setCreated(new Date(System.currentTimeMillis()));
        timeSeries.setVisibility("private");

        timeSeriesRepository.save(timeSeries);
        return timeSeries;
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
        Map<Long, Double> loadedTimeSeries = new HashMap<>();
        String timeSeriesName;
        try (Workbook workbook = new XSSFWorkbook(timeSeriesFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            timeSeriesName = sheet.getRow(0).getCell(1).getStringCellValue();

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                double value = sheet.getRow(i).getCell(0).getNumericCellValue();
                loadedTimeSeries.put((long) (i - 2), value);
            }
        }
        return new TimeSeriesDTO(timeSeriesName, loadedTimeSeries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeSeriesDTO getTimeSeriesDTOFromDatabase(Long timeSeriesId) {
        List<TimeSeriesValue> timeSeriesValueList = timeSeriesValueRepository.findAllByTimeSeriesIdOrderByOrderNo(timeSeriesId);
        Map<Long, Double> timeSeries = new HashMap<>();
        for (int i = 0; i < timeSeriesValueList.size(); i++) {
            timeSeries.put((long) i, timeSeriesValueList.get(i).getValue());
        }
        String name = timeSeriesRepository.findById(timeSeriesId).orElseThrow(() -> new RuntimeException("TimeSeries not found")).getName();
        return new TimeSeriesDTO(name, timeSeries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeSeries getTimeSeriesFromDatabase(Long timeSeriesId) {
        return timeSeriesRepository.findById(timeSeriesId).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTimeSeriesName(Long timeSeriesId, String newName) {
        TimeSeries timeSeries = timeSeriesRepository.findById(timeSeriesId).get();
        timeSeries.setName(newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteTimeSeries(long timeSeriesId) {

        calculationRepository.markMissingInput(timeSeriesId);
        calculationRepository.markMissingOutput(timeSeriesId);

        timeSeriesRepository.deleteById(timeSeriesId);

        calculationRepository.markBrokenWhereBothNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    private void exportTimeSeriesValues(Sheet sheet, Map<Long, Double> timeSeriesValues) {
        for(int i = 0; i < timeSeriesValues.size(); i++) {
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(timeSeriesValues.get((long)i));
        }
    }

}
