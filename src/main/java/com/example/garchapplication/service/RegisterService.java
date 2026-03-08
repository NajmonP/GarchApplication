package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.api.RegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface RegisterService {

    @Transactional(rollbackFor = Exception.class)
    void register(RegisterRequest registerRequest);
}
