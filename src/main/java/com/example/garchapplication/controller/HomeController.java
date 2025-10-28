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
            @RequestParam("time_series_file") MultipartFile timeSeriesFile
    ) {
        calculationService.calculate(startVariance, constantVariance, lastVariance, lastShock, timeSeriesFile);
        return "redirect:/";
    }

    @PostMapping(value = "/start-calculation-configuration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String startCalculationConfiguration(
            @RequestParam("modelId") Long modelId,
            @RequestParam("time_series_file") MultipartFile timeSeriesFile

    ) {
        calculationService.calculateFromSelectedModel(modelId, timeSeriesFile);
        return "redirect:/";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "403";
    }
}
