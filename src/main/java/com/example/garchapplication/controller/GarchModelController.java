package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.api.GarchModelCalculationDTO;
import com.example.garchapplication.service.GarchModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
class GarchModelController {

    private final GarchModelService garchModelService;

    @Autowired
    public GarchModelController(GarchModelService garchModelService) {
        this.garchModelService = garchModelService;
    }

    @PutMapping("/model/{modelId}")
    @ResponseBody
    public ResponseEntity<Void> updateModel(
            @PathVariable Long modelId,
            @RequestBody GarchModelCalculationDTO garchModelCalculationDTO
    ) {
        garchModelService.updateGarchModel(modelId, garchModelCalculationDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/model/{modelId}")
    @ResponseBody
    public ResponseEntity<Void> deleteModel(@PathVariable Long modelId) {
        garchModelService.deleteGarchModel(modelId);
        return ResponseEntity.noContent().build();
    }
}
