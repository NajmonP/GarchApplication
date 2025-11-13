package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.model.entity.TimeSeriesValue;
import com.example.garchapplication.model.entity.User;
import com.example.garchapplication.repository.TimeSeriesRepository;
import com.example.garchapplication.repository.TimeSeriesValueRepository;
import com.example.garchapplication.security.UserDetailsImpl;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TimeSeriesServiceImpl implements TimeSeriesService {

    private final UserService userService;
    private final TimeSeriesRepository timeSeriesRepository;
    private final TimeSeriesValueRepository timeSeriesValueRepository;

    @Autowired
    public TimeSeriesServiceImpl(UserService userService, TimeSeriesRepository timeSeriesRepository, TimeSeriesValueRepository timeSeriesValueRepository) {
        this.userService = userService;
        this.timeSeriesRepository = timeSeriesRepository;
        this.timeSeriesValueRepository = timeSeriesValueRepository;
    }

    @Override
    public List<TimeSeries> getTimeSeriesByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return new ArrayList<>();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long userId = ((UserDetailsImpl) userDetails).getId();
        User user = userService.getUserById(userId);

        return timeSeriesRepository.getTimeSeriesByUser(user);
    }

    /**
     * Saves time series and values data from uploaded to file into database.
     *
     * @param timeSeriesFile uploaded time series file
     * @throws IOException if reading the uploaded time series file fails
     */
    @Override
    public void addTimeSeries(MultipartFile timeSeriesFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(timeSeriesFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            String timeSeriesName = sheet.getRow(0).getCell(1).getStringCellValue();

            TimeSeries timeSeries = saveTimeSeries(timeSeriesName);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                double value = sheet.getRow(i).getCell(0).getNumericCellValue();
                saveTimeSeriesValue(timeSeries, value, i);
            }
        }
    }

    /**
     * Saves time series into database.
     *
     * @param timeSeriesName name of new time series
     * @return saved time series stored in database
     */
    public TimeSeries saveTimeSeries(String timeSeriesName) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = ((UserDetailsImpl) userDetails).getId();
        User user = userService.getUserById(userId);

        TimeSeries timeSeries = new TimeSeries();
        timeSeries.setUser(user);
        timeSeries.setName(timeSeriesName);
        timeSeries.setCreatedAt(new Date(System.currentTimeMillis()));
        timeSeries.setVisibility("private");

        timeSeriesRepository.save(timeSeries);
        return timeSeries;
    }

    /**
     * Save time series value into database.
     *
     * @param timeSeries time series of given value
     * @param value value of next time series observation
     * @param rowNum index of given value
     */
    public void saveTimeSeriesValue(TimeSeries timeSeries, double value, int rowNum) {
        TimeSeriesValue timeSeriesValue = new TimeSeriesValue();
        timeSeriesValue.setTimeSeries(timeSeries);
        timeSeriesValue.setValue(value);
        timeSeriesValue.setOrderNo(rowNum - 1);
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
                loadedTimeSeries.put((long) (i - 1), value);
            }
        }
        return new TimeSeriesDTO(timeSeriesName, loadedTimeSeries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeSeriesDTO getTimeSeriesFromDatabase(Long timeSeriesId) {
        List<TimeSeriesValue> timeSeriesValueList = timeSeriesValueRepository.findAllByTimeSeriesId(timeSeriesId);
        Map<Long, Double> timeSeries = timeSeriesValueList.stream().collect(Collectors.toMap(TimeSeriesValue::getId, TimeSeriesValue::getValue));
        String name = timeSeriesRepository.findById(timeSeriesId).get().getName();
        return new TimeSeriesDTO(name,  timeSeries);
    }
}
