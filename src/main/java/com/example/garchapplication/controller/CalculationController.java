package com.example.garchapplication.controller;

import com.example.garchapplication.model.entity.Calculation;
import com.example.garchapplication.service.CalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CalculationController {
    private final CalculationService calculationService;

    @Autowired
    public CalculationController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @GetMapping("/calculation")
    public String calculation(Model model) {
        List<Calculation> calculationList = calculationService.getAllCalculationsByUser();

        model.addAttribute("calculationList", calculationList);
        return "calculation";
    }
}
