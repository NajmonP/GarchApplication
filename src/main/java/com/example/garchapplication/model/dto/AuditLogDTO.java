package com.example.garchapplication.model.dto;

import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.model.enums.OperationType;

import java.sql.Date;

public record AuditLogDTO(Date occurredAt, long userId, String username, long entityId, EntityType entityType, String entityName, OperationType operationType) {
}
