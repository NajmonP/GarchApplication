package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidDateRangeException;
import com.example.garchapplication.model.dto.api.AuditLogDTO;
import com.example.garchapplication.model.entity.AuditLog;
import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.model.enums.OperationType;
import com.example.garchapplication.repository.AuditLogRepository;
import com.example.garchapplication.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogServiceImpl(ApplicationEventPublisher eventPublisher, AuditLogRepository auditLogRepository) {
        this.eventPublisher = eventPublisher;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void logCreateEvent(EntityType entityType, long entityId, String entityName) {
        publish(new AuditLogDTO(Instant.now(), getUsersId(), getUsersName(), entityId, entityType, entityName, OperationType.CREATE));
    }

    @Override
    public void logUpdateEvent(EntityType entityType, long entityId, String entityName) {
        publish(new AuditLogDTO(Instant.now(), getUsersId(), getUsersName(), entityId, entityType, entityName, OperationType.UPDATE));
    }

    @Override
    public void logDeleteEvent(EntityType entityType, long entityId, String entityName) {
        publish(new AuditLogDTO(Instant.now(), getUsersId(), getUsersName(), entityId, entityType, entityName, OperationType.DELETE));
    }

    @Override
    public Page<AuditLogDTO> findBetween(LocalDate from, LocalDate to, int page, int size) {
        if (from.isAfter(to)) {
            throw new InvalidDateRangeException();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("occuredAt").descending());

        ZoneId zone = ZoneId.of("Europe/Prague");

        Instant fromInstant = from.atStartOfDay(zone).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(zone).toInstant();

        Page<AuditLog> result = auditLogRepository.findByOccuredAtGreaterThanEqualAndOccuredAtLessThan(fromInstant, toInstant, pageable);
        return result.map(a -> new AuditLogDTO(
                a.getOccuredAt(),
                a.getUserId(),
                a.getUsername(),
                a.getEntityId(),
                a.getEntityType(),
                a.getEntityName(),
                a.getOperation()
        ));
    }

    private void publish(AuditLogDTO auditLogDTO) {
        eventPublisher.publishEvent(auditLogDTO);
    }


    private Long getUsersId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof String s) {
            if("anonymousUser".equals(s)){
                return null;
            }
        }

        UserDetails userDetails = (UserDetails) principal;
        return ((UserDetailsImpl) userDetails).getId();
    }

    private String  getUsersName(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof String s) {
            if("anonymousUser".equals(s)){
                return "unauthenticated user";
            }
        }

        UserDetails userDetails = (UserDetails) principal;
        return userDetails.getUsername();
    }
}
