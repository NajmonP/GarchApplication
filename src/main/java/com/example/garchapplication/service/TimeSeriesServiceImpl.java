package com.example.garchapplication.service;

import com.example.garchapplication.model.TimeSeries;
import com.example.garchapplication.model.TimeSeriesValue;
import com.example.garchapplication.model.User;
import com.example.garchapplication.repository.TimeSeriesRepository;
import com.example.garchapplication.repository.TimeSeriesValueRepository;
import com.example.garchapplication.security.UserDetailsImpl;
import org.apache.poi.ss.usermodel.Row;
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
import java.util.List;

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

    @Override
    public void addTimeSeries(MultipartFile timeSeriesFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(timeSeriesFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            String timeSeriesName = sheet.getRow(0).getCell(1).getStringCellValue();

            TimeSeries timeSeries = saveTimeSeries(timeSeriesName);
            for(int i = 2; i < sheet.getLastRowNum(); i++) {
                saveTimeSeriesValue(timeSeries, sheet.getRow(i), i);
            }
        }
    }

    private TimeSeries saveTimeSeries(String timeSeriesName){
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

    private void saveTimeSeriesValue(TimeSeries timeSeries, Row row, int rowNum) {
        TimeSeriesValue timeSeriesValue = new TimeSeriesValue();
        timeSeriesValue.setTimeSeries(timeSeries);
        timeSeriesValue.setValue(row.getCell(0).getNumericCellValue());
        timeSeriesValue.setOrderNo(rowNum-1);
        timeSeriesValueRepository.save(timeSeriesValue);
    }
}
