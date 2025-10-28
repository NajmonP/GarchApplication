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

    @PostMapping(value = "/time-series/add-time-series", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addTimeSeries(
            @RequestParam("time-series") MultipartFile timeSeriesFile
    ) throws IOException {
        timeSeriesService.addTimeSeries(timeSeriesFile);
        return "redirect:/time-series";
    }
}


