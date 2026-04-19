package com.example.garchapplication.helper;

import com.example.garchapplication.model.dto.api.AuditLogDTO;
import com.example.garchapplication.model.entity.AuditLog;
import com.example.garchapplication.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditLogListener {
    private final AuditLogRepository auditLogRepository;;

    @Autowired
    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuditLogEvent(AuditLogDTO auditLogDTO) {
        AuditLog auditLog = new AuditLog();

        auditLog.setOccuredAt(auditLogDTO.occurredAt());
        auditLog.setUserId(auditLogDTO.userId());
        auditLog.setUsername(auditLogDTO.username());
        auditLog.setEntityType(auditLogDTO.entityType());
        auditLog.setEntityName(auditLogDTO.entityName());
        auditLog.setEntityId(auditLogDTO.entityId());
        auditLog.setOperation(auditLogDTO.operationType());
        auditLogRepository.save(auditLog);
    }
}
