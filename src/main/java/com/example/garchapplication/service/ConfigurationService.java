package com.example.garchapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface ConfigurationService {
    public void addConfiguration(MultipartFile configurationFile) throws IOException;
}
