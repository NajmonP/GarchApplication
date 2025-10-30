package com.example.garchapplication.service;

import com.example.garchapplication.dto.GarchModelDTO;
import com.example.garchapplication.model.GarchModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GarchModelService {
    List<GarchModel> findAllByConfigurationId(Long configurationId);

    GarchModelDTO extractGarchModelDTO(Long modelId);
}
