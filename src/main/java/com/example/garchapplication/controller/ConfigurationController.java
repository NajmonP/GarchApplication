package com.example.garchapplication.controller;

import com.example.garchapplication.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
class ConfigurationController {

    private final ConfigurationService configurationService;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/configuration")
    public String configuration() {
        return "configuration";
    }

    /**
     * Handles POST requests to add a new configuration file containing GARCH models.
     * <br>
     * The uploaded XLSX file is processed and saved using {@link ConfigurationService}.
     *
     * @param configurationFile the uploaded XLSX configuration file containing GARCH models
     * @return redirect to the configuration page
     * @throws IOException if reading or processing the uploaded file fails
     */
    @PostMapping(value = "/configuration/add-configuration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addConfiguration(
            @RequestParam("configuration") MultipartFile configurationFile
    ) throws IOException {
        configurationService.addConfiguration(configurationFile);
        return "redirect:/configuration";
    }
}
