package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.api.GarchModelCalculationDTO;
import com.example.garchapplication.model.dto.api.CalculationDetailDTO;
import com.example.garchapplication.model.dto.api.CalculationPageDTO;
import com.example.garchapplication.model.dto.api.ChartOfTimeSeriesDTO;
import com.example.garchapplication.service.CalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class CalculationController {
    private final CalculationService calculationService;

    @Autowired
    public CalculationController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @GetMapping("/calculation")
    public String calculation() {
        return "calculation";
    }

    @GetMapping("/calculation/data")
    @ResponseBody
    public CalculationPageDTO calculationData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return calculationService.getCalculationPageByUser(page, size);
    }

    @GetMapping("/calculation/{calculationId}")
    public String calculationDetailPage(@PathVariable long calculationId, Model model){
        model.addAttribute("calculationId", calculationId);
        return "calculation-detail";
    }

    @GetMapping("/calculation/detail/{calculationId}")
    @ResponseBody
    public CalculationDetailDTO calculationDetail(@PathVariable long calculationId) {
        return calculationService.getCalculationDetails(calculationId);
    }

    @DeleteMapping("/calculation/{calculationId}")
    @ResponseBody
    public ResponseEntity<Void> deleteCalculation(@PathVariable Long calculationId) {
        calculationService.deleteCalculation(calculationId);
        return ResponseEntity.noContent().build();
    }


    /**
     * Starts a GARCH calculation using user input.
     * <br>
     * Calculation is started using either inserted time series file or selected time series already stored in database.
     *
     * @param startVariance    first variance from which the calculation starts
     * @param constantVariance the constant variance
     * @param lastVariance     array containing weights of last variances
     * @param lastShock        array containing weights of last shocks
     * @param timeSeriesFile   an optional uploaded time series file (may be null)
     * @param timeSeriesId     an optional ID of an existing time series (may be null)
     * @return a redirect to the home page after calculation completion
     * @throws IOException if reading the uploaded time series file fails
     */
    @PostMapping(value = "/start-calculation-manual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ChartOfTimeSeriesDTO startCalculationManual(
            @RequestParam("start_variance") double startVariance,
            @RequestParam("constant_variance") double constantVariance,
            @RequestParam("last_variance[]") List<Double> lastVariance,
            @RequestParam("last_shock[]") List<Double> lastShock,
            @RequestParam(value = "time_series_file", required = false) MultipartFile timeSeriesFile,
            @RequestParam(value = "forecast", required = true) int forecast,
            @RequestParam(value = "timeSeriesId", required = false) Long timeSeriesId
    ) throws IOException {
        GarchModelCalculationDTO garchModelCalculationDTO = new GarchModelCalculationDTO( "name", startVariance, constantVariance, lastVariance, lastShock);

        return calculationService.calculateAndPrepareGraph(garchModelCalculationDTO, forecast, timeSeriesFile, timeSeriesId, null);
    }

    /**
     * Starts a GARCH calculation using a selected model from configuration.
     * <br>
     * Calculation is started using either inserted time series file or selected time series already stored in database.
     *
     * @param modelId        the ID of the selected GARCH model to use
     * @param timeSeriesFile an optional uploaded time series file (may be null)
     * @param timeSeriesId   an optional ID of an existing time series (may be null)
     * @return a redirect to the home page after calculation completion
     * @throws IOException if reading the uploaded time series file fails
     */
    @PostMapping(value = "/start-calculation-configuration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ChartOfTimeSeriesDTO startCalculationConfiguration(
            @RequestParam("modelId") Long modelId,
            @RequestParam(value = "time_series_file", required = false) MultipartFile timeSeriesFile,
            @RequestParam(value = "forecast_config", required = true) int forecast,
            @RequestParam(value = "timeSeriesId", required = false) Long timeSeriesId

    ) throws IOException {
        return calculationService.calculateFromSelectedModel(modelId, forecast, timeSeriesFile, timeSeriesId);
    }

    @PostMapping("/calculation/{calculationId}/rerun")
    public ResponseEntity<Void> rerunCalculation(
            @PathVariable long calculationId,
            @RequestBody Long timeSeriesId
    ) throws IOException {
        calculationService.rerunCalculation(calculationId, timeSeriesId);
        return ResponseEntity.noContent().build();
    }
}
