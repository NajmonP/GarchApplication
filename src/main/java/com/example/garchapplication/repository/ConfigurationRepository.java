package com.example.garchapplication.repository;

import com.example.garchapplication.model.dto.AuditInfoDTO;
import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    @Query("select c.name from Configuration c where c.id = :id")
    Optional<String> findNameById(@Param("id") long id);

    List<Configuration> getConfigurationsByUser(User user);

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("""
                select new com.example.garchapplication.model.dto.AuditInfoDTO(
                com.example.garchapplication.model.enums.EntityType.CONFIGURATION,
                c.id,
                c.name)
                from Configuration c
                where c.user.id = :userId
            """)
    List<AuditInfoDTO> findAllAuditInfoByUserId(Long userId);
}
