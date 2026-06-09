package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.api.AuditLogDTO;
import com.example.garchapplication.model.entity.AuditLog;
import org.springframework.data.domain.Page;

public final class AuditLogMapper {
    public static Page<AuditLogDTO> toAuditLogDTO(Page<AuditLog> pageOfAuditLogs) {
        return pageOfAuditLogs.map(a -> new AuditLogDTO(
                a.getOccuredAt(),
                a.getUserId(),
                a.getUsername(),
                a.getEntityId(),
                a.getEntityType(),
                a.getEntityName(),
                a.getOperation()
        ));
    }
}
