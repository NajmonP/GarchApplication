package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByOccuredAtGreaterThanEqualAndOccuredAtLessThan(
            Instant from,
            Instant toExclusive,
            Pageable pageable
    );
}
