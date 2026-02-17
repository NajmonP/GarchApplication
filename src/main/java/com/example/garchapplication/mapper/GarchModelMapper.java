package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.GarchModelDTO;
import com.example.garchapplication.model.dto.GarchModelCalculationDTO;
import com.example.garchapplication.model.entity.GarchModel;
import com.example.garchapplication.model.entity.ModelShockWeight;
import com.example.garchapplication.model.entity.ModelVarianceWeight;

import java.util.List;

public class GarchModelMapper {

    public static GarchModelCalculationDTO toGarchModelCalculationDTO(
            GarchModel model,
            List<ModelVarianceWeight> varianceWeights,
            List<ModelShockWeight> shockWeights
    ) {

        List<Double> lastVariances = varianceWeights.stream()
                .map(ModelVarianceWeight::getValue)
                .toList();

        List<Double> lastShocks = shockWeights.stream()
                .map(ModelShockWeight::getValue)
                .toList();

        return new GarchModelCalculationDTO(
                model.getName(),
                model.getStartVariance(),
                model.getConstantVariance(),
                lastVariances,
                lastShocks
        );
    }

    public static GarchModelDTO toGarchModelDTO(
            GarchModel model,
            List<ModelVarianceWeight> varianceWeights,
            List<ModelShockWeight> shockWeights
    ) {

        List<Double> lastVariances = varianceWeights.stream()
                .map(ModelVarianceWeight::getValue)
                .toList();

        List<Double> lastShocks = shockWeights.stream()
                .map(ModelShockWeight::getValue)
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
