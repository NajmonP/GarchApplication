package com.example.garchapplication.model.dto.api;

import com.example.garchapplication.model.enums.EntityType;

public record AuditInfoDTO(EntityType type, Long id, String name) {
}
