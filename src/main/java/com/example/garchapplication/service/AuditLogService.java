package com.example.garchapplication.service;

import com.example.garchapplication.model.enums.EntityType;
import org.springframework.stereotype.Service;

@Service
public interface AuditLogService {

    void logCreateEvent(EntityType entityType, long entityId, String entityName);

    void logUpdateEvent(EntityType entityType, long entityId, String entityName);

    void logDeleteEvent(EntityType entityType, long entityId, String entityName);
}
