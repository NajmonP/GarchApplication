package com.example.garchapplication.service;

import com.example.garchapplication.dto.GarchModelDTO;
import com.example.garchapplication.model.GarchModel;
import com.example.garchapplication.model.ModelShockWeight;
import com.example.garchapplication.model.ModelVarianceWeight;
import com.example.garchapplication.repository.GarchModelRepository;
import com.example.garchapplication.repository.ModelShockWeightRepository;
import com.example.garchapplication.repository.ModelVarianceWeightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GarchModelServiceImpl implements GarchModelService {

    private final GarchModelRepository garchModelRepository;
    private final ModelVarianceWeightRepository modelVarianceWeightRepository;
    private final ModelShockWeightRepository modelShockWeightRepository;

    @Autowired
    public GarchModelServiceImpl(GarchModelRepository garchModelRepository, ModelVarianceWeightRepository modelVarianceWeightRepository, ModelShockWeightRepository modelShockWeightRepository) {
        this.garchModelRepository = garchModelRepository;
        this.modelVarianceWeightRepository = modelVarianceWeightRepository;
        this.modelShockWeightRepository = modelShockWeightRepository;
    }


    @Override
    public List<GarchModel> findAllByConfigurationId(Long configurationId) {
        return garchModelRepository.findAllByConfigurationId(configurationId);
    }

    @Override
    public GarchModelDTO extractGarchModelDTO(Long modelId) {
        GarchModel garchModel = garchModelRepository.findById(modelId).orElse(null);
        String modelName = garchModel.getName();

        List<ModelVarianceWeight> modelVarianceWeightList = modelVarianceWeightRepository.findAllByGarchModelId(garchModel.getId());
        List<ModelShockWeight> modelShockWeightList = modelShockWeightRepository.findAllByGarchModelId(garchModel.getId());

        double startVariance = garchModel.getStartVariance();
        double constantVariance = garchModel.getConstantVariance();
        List<Double> lastVariances = new ArrayList<>();
        List<Double> lastShocks = new ArrayList<>();
        for (ModelVarianceWeight modelVarianceWeight : modelVarianceWeightList) {
            lastVariances.add(modelVarianceWeight.getValue());
        }
        for (ModelShockWeight modelShockWeight : modelShockWeightList) {
            lastShocks.add(modelShockWeight.getValue());
        }

        return new GarchModelDTO(modelName, startVariance, constantVariance, lastVariances, lastShocks);
    }
}
