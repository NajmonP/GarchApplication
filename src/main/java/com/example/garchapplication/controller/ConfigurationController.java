package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.dto.UpdateConfigurationRequest;
import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.service.ConfigurationService;
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
class ConfigurationController {

    private final ConfigurationService configurationService;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/configuration")
    public String configuration(Model model) {
        List<Configuration> configurationlist = configurationService.getAllConfigurationsByUser();

        model.addAttribute("configurationList", configurationlist);
        return "configuration";
    }

    /**
     * Displays all GARCH models of selected configuration.
     *
     * @param configurationId ID of selected configuration
     * @return List of all GARCH models of selected configuration
     */
    @GetMapping("/configuration/{configurationId}")
    @ResponseBody
    public List<GarchModelDTO> getModelsByConfiguration(@PathVariable Long configurationId) {
        return configurationService.extractGarchModelDTOsByConfigurationId(configurationId);
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

    @PutMapping("/configuration/{configurationId}")
    @ResponseBody
    public ResponseEntity<Void> updateConfiguration(
            @PathVariable Long configurationId,
            @RequestBody UpdateConfigurationRequest updateConfigurationRequest
    ) {
        configurationService.updateConfigurationName(configurationId, updateConfigurationRequest.name());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/configuration/{configurationId}")
    @ResponseBody
    public ResponseEntity<Void> deleteConfiguration(@PathVariable Long configurationId) {
        configurationService.deleteConfiguration(configurationId);
        return ResponseEntity.noContent().build();
    }

}
