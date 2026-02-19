package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.dto.XlsxFileDTO;
import com.example.garchapplication.model.entity.TimeSeries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface TimeSeriesService {

    /**
     * Saves time series and values data from uploaded to file into database.
     *
     * @param timeSeriesFile uploaded time series file
     * @throws IOException if reading the uploaded time series file fails
     */
    @Transactional(rollbackFor = Exception.class)
    void addTimeSeriesFromFile(MultipartFile timeSeriesFile) throws IOException;

    /**
     * Saves time series and values data from given time series DTO.
     *
     * @param timeSeriesDTO DTO of given time series
     * @return object of saved time series
     */
    @Transactional(rollbackFor = Exception.class)
    TimeSeries addTimeSeriesFromDTO(TimeSeriesDTO timeSeriesDTO);

    List<TimeSeries> getTimeSeriesByUser();

    /**
     * Loads time series data from uploaded time series file.
     *
     * @param timeSeriesFile uploaded time series file from user input
     * @return DTO of uploaded time series
     * @throws IOException if reading the uploaded time series file fails
     */
    TimeSeriesDTO getTimeSeriesFromFile(MultipartFile timeSeriesFile) throws IOException;

    /**
     * Loads time series data from database based on user input and maps the result into DTO.
     *
     * @param timeSeriesId ID of selected time series
     * @return DTO of selected time series
     */
    TimeSeriesDTO getTimeSeriesDTOFromDatabase(Long timeSeriesId);

    /**
     * Loads time series data from database based on user input.
     *
     * @param timeSeriesId ID of selected time series
     * @return selected time series
     */
    TimeSeries getTimeSeriesFromDatabase(Long timeSeriesId);

    /**
     * Updates time series name.
     *
     * @param timeSeriesId id of time series that is going to be updated
     * @param newName new name of time series
     */
    @Transactional(rollbackFor = Exception.class)
    void updateTimeSeriesName(Long timeSeriesId, String newName);


    /**
     * Deletes time series.
     *
     * @param timeSeriesId id of time series that is going to be deleted
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteTimeSeries(long timeSeriesId);

    /**
     * Loads all time series data from database and writes them into xlsx file.
     *
     * @param timeSeriesId id of time series that is going to be exported
     * @return DTO containing time series name and corresponding data
     */
    XlsxFileDTO exportTimeSeries(long timeSeriesId);
}
