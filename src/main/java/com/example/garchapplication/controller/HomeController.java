package com.example.garchapplication.controller;

import com.example.garchapplication.model.Configuration;
import com.example.garchapplication.model.GarchModel;
import com.example.garchapplication.model.TimeSeries;
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

    @GetMapping("/")
    String home(Model model) {
        List<Configuration> configurationlist = configurationService.getAllConfigurationsByUser();
        List<TimeSeries> timeSeriesList = timeSeriesService.getTimeSeriesByUser();

        model.addAttribute("configurationList", configurationlist);
        model.addAttribute("timeSeriesList", timeSeriesList);
        return "index";
    }

    @GetMapping("/configuration/{configurationId}")
    @ResponseBody
    public List<GarchModel> getModelsByConfiguration(@PathVariable Long configurationId) {
        return garchModelService.findAllByConfigurationId(configurationId);
    }

    @PostMapping(value = "/start-calculation-manual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String startCalculationManual(
            @RequestParam("start_variance") double startVariance,
            @RequestParam("constant_variance") double constantVariance,
            @RequestParam("last_variance[]") List<Double> lastVariance,
            @RequestParam("last_shock[]") List<Double> lastShock,
            @RequestParam(value = "time_series_file", required = false) MultipartFile timeSeriesFile,
            @RequestParam(value = "timeSeriesId", required = false) Long timeSeriesId
    ) throws IOException {
        calculationService.calculate(startVariance, constantVariance, lastVariance, lastShock, timeSeriesFile, timeSeriesId);
        return "redirect:/";
    }

    @PostMapping(value = "/start-calculation-configuration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String startCalculationConfiguration(
            @RequestParam("modelId") Long modelId,
            @RequestParam(value = "time_series_file", required = false) MultipartFile timeSeriesFile,
            @RequestParam(value = "timeSeriesId", required = false) Long timeSeriesId

    ) throws IOException {
        calculationService.calculateFromSelectedModel(modelId, timeSeriesFile, timeSeriesId);
        return "redirect:/";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "403";
    }
}
