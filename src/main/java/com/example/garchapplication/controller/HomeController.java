package com.example.garchapplication.controller;

import com.example.garchapplication.mapper.TimeSeriesChartMapper;
import com.example.garchapplication.model.dto.ChartOfTimeSeriesDTO;
import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.dto.TimeSeriesDTO;
import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.model.entity.GarchModel;
import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.service.ConfigurationService;
import com.example.garchapplication.service.GarchModelService;
import com.example.garchapplication.service.CalculationService;
import com.example.garchapplication.service.TimeSeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
class HomeController {

    private final CalculationService calculationService;
    private final ConfigurationService configurationService;
    private final GarchModelService garchModelService;
    private final TimeSeriesService timeSeriesService;

    @Autowired
    HomeController(CalculationService calculationService, ConfigurationService configurationService, GarchModelService garchModelService, TimeSeriesService timeSeriesService) {
        this.calculationService = calculationService;
        this.configurationService = configurationService;
        this.garchModelService = garchModelService;
        this.timeSeriesService = timeSeriesService;
    }

    /**
     * Displays the home page with all configurations and time series associated with the current user.
     *
     * @param model the model used to pass configuration and time series data to the view
     * @return the name of the home page view ("index")
     */
    @GetMapping("/")
    String home(Model model) {
        List<Configuration> configurationlist = configurationService.getAllConfigurationsByUser();
        List<TimeSeries> timeSeriesList = timeSeriesService.getTimeSeriesByUser();

        model.addAttribute("configurationList", configurationlist);
        model.addAttribute("timeSeriesList", timeSeriesList);
        return "index";
    }

    /**
     * Displays all GARCH models of selected configuration.
     *
     * @param configurationId ID of selected configuration
     * @return List of all GARCH models of selected configuration
     */
    @GetMapping("/configuration/{configurationId}")
    @ResponseBody
    public List<GarchModel> getModelsByConfiguration(@PathVariable Long configurationId) {
        return garchModelService.findAllByConfigurationId(configurationId);
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
            @RequestParam(value = "timeSeriesId", required = false) Long timeSeriesId
    ) throws IOException {
        GarchModelDTO garchModelDTO = new GarchModelDTO("name", startVariance, constantVariance, lastVariance, lastShock);

        TimeSeriesDTO result = calculationService.calculate(garchModelDTO, timeSeriesFile, timeSeriesId);
        return TimeSeriesChartMapper.toChart(result);
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
            @RequestParam(value = "timeSeriesId", required = false) Long timeSeriesId

    ) throws IOException {
        TimeSeriesDTO result = calculationService.calculateFromSelectedModel(modelId, timeSeriesFile, timeSeriesId);
        return TimeSeriesChartMapper.toChart(result);
    }
}
