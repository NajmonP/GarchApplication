package com.example.garchapplication.service;

import com.example.garchapplication.model.Configuration;
import com.example.garchapplication.model.GarchModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface ConfigurationService {
    public void addConfiguration(MultipartFile configurationFile) throws IOException;

    public List<Configuration> getAllConfigurationsByUser();
}
