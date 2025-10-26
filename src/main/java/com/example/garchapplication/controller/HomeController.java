package com.example.garchapplication.controller;

import com.example.garchapplication.model.Configuration;
import com.example.garchapplication.service.ConfigurationService;
import com.example.garchapplication.service.GarchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/")
class HomeController {

    private final GarchService garchService;
    private final ConfigurationService configurationService;

    @Autowired
    HomeController(GarchService garchService, ConfigurationService configurationService) {
        this.garchService = garchService;
        this.configurationService = configurationService;
    }

    @GetMapping("/")
    String home(Model model) {
        List<Configuration> configurationlist = configurationService.getAllConfigurationsByUser();
        model.addAttribute("configurationList", configurationlist);
        return "index";
    }

    @PostMapping(value = "/start-calculation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String startCalculation(
            @RequestParam("start_variance") double startVariance,
            @RequestParam("constant_variance") double constantVariance,
            @RequestParam("last_variance[]") List<Double> lastVariance,
            @RequestParam("last_shock[]") List<Double> lastShock,
            @RequestParam("time_series_file") MultipartFile timeSeriesFile
    ) {
        garchService.calculate(startVariance, constantVariance, lastVariance, lastShock, timeSeriesFile);
        return "redirect:/";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "403";
    }
}
