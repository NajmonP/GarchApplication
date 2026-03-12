package com.example.garchapplication.controller;

import com.example.garchapplication.helper.DownloadHeaderUtil;
import com.example.garchapplication.model.dto.*;
import com.example.garchapplication.model.dto.api.TimeSeriesDetailDTO;
import com.example.garchapplication.model.dto.api.TimeSeriesListItemDTO;
import com.example.garchapplication.model.dto.api.TimeSeriesPageDTO;
import com.example.garchapplication.model.dto.api.UpdateTimeSeriesRequest;
import com.example.garchapplication.service.TimeSeriesService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
    public String timeSeries() {
        return "time-series";
    }

    @GetMapping("/time-series/data")
    @ResponseBody
    public TimeSeriesPageDTO timeSeriesData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return timeSeriesService.getTimeSeriesPageByUser(page, size);
    }

    @GetMapping("/time-series/select/data")
    @ResponseBody
    public List<TimeSeriesListItemDTO> timeSeriesData() {
        return timeSeriesService.getTimeSeriesByUser();
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
    @ResponseBody
    public ResponseEntity<Void> addTimeSeries(
            @RequestParam("time-series") MultipartFile timeSeriesFile
    ) throws IOException {
        timeSeriesService.addTimeSeriesFromFile(timeSeriesFile);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/time-series/{timeSeriesId}")
    @ResponseBody
    public ResponseEntity<Void> updateTimeSeriesName(@PathVariable long timeSeriesId,
                                                     @RequestBody UpdateTimeSeriesRequest updateTimeSeriesRequest) {
        timeSeriesService.updateTimeSeries(timeSeriesId, updateTimeSeriesRequest);
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
    @ResponseBody
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

    @GetMapping("/time-series/download/sample")
    public ResponseEntity<Resource> downloadSampleTimeSeries() {

        String downloadName = "Time-series-sample";
        ContentDisposition contentDisposition = DownloadHeaderUtil.createExcelAttachment(downloadName);

        Resource resource = timeSeriesService.downloadSampleTimeSeries();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(resource);
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


