package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.api.GarchModelDTO;
import com.example.garchapplication.model.dto.api.GarchModelCalculationDTO;
import com.example.garchapplication.model.entity.*;

import java.util.List;

public final class GarchModelMapper {

    public static GarchModelCalculationDTO toGarchModelCalculationDTO(
            GarchModel model,
            List<ModelAlpha> varianceWeights,
            List<ModelBeta> shockWeights
    ) {

        List<Double> lastVariances = varianceWeights.stream()
                .map(ModelAlpha::getValue)
                .toList();

        List<Double> lastShocks = shockWeights.stream()
                .map(ModelBeta::getValue)
                .toList();

        return new GarchModelCalculationDTO(
                model.getName(),
                model.getStartVariance(),
                model.getConstantVariance(),
                lastVariances,
                lastShocks
        );
    }

    public static GarchModelCalculationDTO toGarchModelCalculationDTO(
            Calculation calculation,
            List<RunAlpha> varianceWeights,
            List<RunBeta> shockWeights
    ) {

        List<Double> lastVariances = varianceWeights.stream()
                .map(RunAlpha::getValue)
                .toList();

        List<Double> lastShocks = shockWeights.stream()
                .map(RunBeta::getValue)
                .toList();

        return new GarchModelCalculationDTO(
                "Rerun",
                calculation.getStartVariance(),
                calculation.getConstantVariance(),
                lastVariances,
                lastShocks
        );
    }

    public static GarchModelDTO toGarchModelDTO(
            GarchModel model,
            List<ModelAlpha> varianceWeights,
            List<ModelBeta> shockWeights
    ) {

        List<Double> lastVariances = varianceWeights.stream()
                .map(ModelAlpha::getValue)
                .toList();

        List<Double> lastShocks = shockWeights.stream()
                .map(ModelBeta::getValue)
                .toList();

        return new GarchModelDTO(
                model.getId(),
                model.getName(),
                model.getStartVariance(),
                model.getConstantVariance(),
                lastVariances,
                lastShocks
        );
    }
}
