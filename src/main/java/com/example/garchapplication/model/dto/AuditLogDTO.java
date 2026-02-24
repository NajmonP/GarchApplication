package com.example.garchapplication.model.dto;

import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.model.enums.OperationType;

import java.time.Instant;

public record AuditLogDTO(Instant occurredAt, long userId, String username, long entityId, EntityType entityType, String entityName, OperationType operationType) {
}
