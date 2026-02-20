package com.example.garchapplication.controller;

import com.example.garchapplication.helper.DownloadHeaderUtil;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.dto.TimeSeriesDetailDTO;
import com.example.garchapplication.model.dto.UpdateNameRequest;
import com.example.garchapplication.model.dto.XlsxFileDTO;
import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.service.TimeSeriesService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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

    /**
     * @param timeSeriesId
     * @return
     */
    @GetMapping("/time-series/download/{timeSeriesId}")
    public ResponseEntity<byte[]> downloadTimeSeries(@PathVariable long timeSeriesId) {
        XlsxFileDTO xlsxFileDTO = timeSeriesService.exportTimeSeries(timeSeriesId);

        ContentDisposition contentDisposition = DownloadHeaderUtil.createExcelAttachment(xlsxFileDTO.name());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(xlsxFileDTO.bytes());
    }

    @GetMapping("/time-series/{timeSeriesId}")
    public String timeSeriesDetailPage(@PathVariable long timeSeriesId, Model model) {
        model.addAttribute("timeSeriesId", timeSeriesId);
        return "time-series-detail";
    }

    @GetMapping("/time-series/detail/{timeSeriesId}")
    @ResponseBody
    public TimeSeriesDetailDTO timeSeriesDetail(@PathVariable long timeSeriesId) {
        return timeSeriesService.getTimeSeriesDetails(timeSeriesId);
    }
}


