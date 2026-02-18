package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.UpdateNameRequest;
import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.service.TimeSeriesService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class TimeSeriesController {

    private final TimeSeriesService timeSeriesService;

    public TimeSeriesController(TimeSeriesService timeSeriesService) {
        this.timeSeriesService = timeSeriesService;
    }

    @GetMapping("/time-series")
    public String timeSeries(Model model) {
        List<TimeSeries> timeSeriesList = timeSeriesService.getTimeSeriesByUser();

        model.addAttribute("timeSeriesList", timeSeriesList);
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

    @PutMapping("/time-series/{timeSeriesId}")
    @ResponseBody
    public ResponseEntity<Void> updateTimeSeriesName(@PathVariable long timeSeriesId,
                                                     @RequestBody UpdateNameRequest updateNameRequest) {
        timeSeriesService.updateTimeSeriesName(timeSeriesId, updateNameRequest.name());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/time-series/{timeSeriesId}")
    @ResponseBody
    public ResponseEntity<Void> deleteTimeSeries(@PathVariable long timeSeriesId) {
        timeSeriesService.deleteTimeSeries(timeSeriesId);
        return ResponseEntity.noContent().build();
    }
}


