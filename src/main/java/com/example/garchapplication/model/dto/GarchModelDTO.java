package com.example.garchapplication.model.dto;

import java.util.List;

public record GarchModelDTO(String name, double startVariance, double constantVariance,
                     List<Double> lastVariances, List<Double> lastShocks) {}
