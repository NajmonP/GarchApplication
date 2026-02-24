package com.example.garchapplication.helper;

import com.example.garchapplication.model.dto.AuditLogDTO;
import com.example.garchapplication.model.entity.AuditLog;
import com.example.garchapplication.repository.AuditLogRepository;
import com.example.garchapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditLogListener {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuditLogListener(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuditLogEvent(AuditLogDTO auditLogDTO) {
        AuditLog auditLog = new AuditLog();

        auditLog.setOccuredAt(auditLogDTO.occurredAt());
        auditLog.setUser(userRepository.findById(auditLogDTO.userId()).orElse(null));
        auditLog.setUsername(auditLogDTO.username());
        auditLog.setEntityType(auditLogDTO.entityType());
        auditLog.setEntityName(auditLogDTO.entityName());
        auditLog.setEntityId(auditLogDTO.entityId());
        auditLog.setOperation(auditLogDTO.operationType());
        auditLogRepository.save(auditLog);
    }
}
