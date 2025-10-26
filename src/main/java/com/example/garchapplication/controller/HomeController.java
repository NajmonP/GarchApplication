package com.example.garchapplication.controller;

import com.example.garchapplication.service.GarchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/")
class HomeController {

    private final GarchServiceImpl garchService;

    @Autowired
    HomeController(GarchServiceImpl garchService) {
        this.garchService = garchService;
    }

    @GetMapping("/")
    String home() {
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
