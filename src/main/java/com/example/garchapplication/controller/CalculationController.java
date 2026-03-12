package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.CalculationDetailDTO;
import com.example.garchapplication.model.dto.api.CalculationListItemDTO;
import com.example.garchapplication.model.dto.api.CalculationPageDTO;
import com.example.garchapplication.service.CalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
}
