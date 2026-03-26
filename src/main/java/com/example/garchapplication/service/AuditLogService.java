package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.api.AuditLogDTO;
import com.example.garchapplication.model.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface AuditLogService {

    void logCreateEvent(EntityType entityType, long entityId, String entityName);

    void logUpdateEvent(EntityType entityType, long entityId, String entityName);

    void logDeleteEvent(EntityType entityType, long entityId, String entityName);

    Page<AuditLogDTO> findBetween(LocalDate from, LocalDate to, int page, int size);
}
