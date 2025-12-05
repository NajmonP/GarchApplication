package com.example.garchapplication.controller;

import com.example.garchapplication.service.TimeSeriesService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class TimeSeriesController {

    private final TimeSeriesService timeSeriesService;

    public TimeSeriesController(TimeSeriesService timeSeriesService) {
        this.timeSeriesService = timeSeriesService;
    }

    @GetMapping("/time-series")
    public String timeSeries() {
        return "time-series";
    }

    /**
     * Handles POST requests to add new time series file.
     * <br>
     * The uploaded XLSX file is processed and saved using {@link TimeSeriesService}.
     *
     * @param timeSeriesFile the uploaded XLSX time series file
     * @return redirect to time series page
     * @throws IOException if reading or processing the uploaded file fails
     */
    @PostMapping(value = "/time-series/add-time-series", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addTimeSeries(
            @RequestParam("time-series") MultipartFile timeSeriesFile
    ) throws IOException {
        timeSeriesService.addTimeSeriesFromFile(timeSeriesFile);
        return "redirect:/time-series";
    }
}


