package com.example.garchapplication.service;

import com.example.garchapplication.model.GarchModel;
import com.example.garchapplication.repository.GarchModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GarchModelServiceImpl implements GarchModelService {

    private final GarchModelRepository garchModelRepository;

    @Autowired
    public GarchModelServiceImpl(GarchModelRepository garchModelRepository) {
        this.garchModelRepository = garchModelRepository;
    }


    @Override
    public List<GarchModel> findAllByConfigurationId(Long configurationId) {
        return garchModelRepository.findAllByConfigurationId(configurationId);
    }
}
