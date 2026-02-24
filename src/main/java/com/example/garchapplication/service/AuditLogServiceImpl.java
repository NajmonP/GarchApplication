package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.AuditLogDTO;
import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.model.enums.OperationType;
import com.example.garchapplication.security.AuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationHandler authenticationHandler;

    @Autowired
    public AuditLogServiceImpl(ApplicationEventPublisher eventPublisher, AuthenticationHandler authenticationHandler) {
        this.eventPublisher = eventPublisher;
        this.authenticationHandler = authenticationHandler;
    }

    @Override
    public void logCreateEvent(EntityType entityType, long entityId, String entityName) {
        publish(new AuditLogDTO(Instant.now(), authenticationHandler.getUserEntity().getId(), authenticationHandler.getUserEntity().getUsername(), entityId, entityType, entityName, OperationType.CREATE));
    }

    @Override
    public void logUpdateEvent(EntityType entityType, long entityId, String entityName) {
        publish(new AuditLogDTO(Instant.now(), authenticationHandler.getUserEntity().getId(), authenticationHandler.getUserEntity().getUsername(), entityId, entityType, entityName, OperationType.UPDATE));
    }

    @Override
    public void logDeleteEvent(EntityType entityType, long entityId, String entityName) {
        publish(new AuditLogDTO(Instant.now(), authenticationHandler.getUserEntity().getId(), authenticationHandler.getUserEntity().getUsername(), entityId, entityType, entityName, OperationType.DELETE));
    }

    private void publish(AuditLogDTO auditLogDTO) {
        eventPublisher.publishEvent(auditLogDTO);
    }
}
