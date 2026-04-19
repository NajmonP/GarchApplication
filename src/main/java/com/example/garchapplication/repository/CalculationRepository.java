package com.example.garchapplication.repository;

import com.example.garchapplication.model.dto.AuditInfoDTO;
import com.example.garchapplication.model.entity.Calculation;
import com.example.garchapplication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationRepository extends JpaRepository<Calculation, Long> {
    List<Calculation> getCalculationsByUser(User user);

    @Modifying
    @Query(value = """
                update garch.calculation
                set status = cast('MISSING_INPUT_SERIES' as garch.calculation_status)
                where input_time_series_id = :tsId
            """, nativeQuery = true)
    int markMissingInput(@Param("tsId") long tsId);

    @Modifying
    @Query(value = """
                update garch.calculation
                set status = cast('MISSING_OUTPUT_SERIES' as garch.calculation_status)
                where result_time_series_id = :tsId
            """, nativeQuery = true)
    int markMissingOutput(@Param("tsId") long tsId);

    @Modifying
    @Query(value = """
                update garch.calculation
                set status = cast('BROKEN' as garch.calculation_status)
                where input_time_series_id is null
                  and result_time_series_id is null
            """, nativeQuery = true)
    int markBrokenWhereBothNull();

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("""
                select new com.example.garchapplication.model.dto.AuditInfoDTO(
                    com.example.garchapplication.model.enums.EntityType.CALCULATION,
                    c.id,
                    null
                )
                from Calculation c
                where c.user.id = :userId
            """)
    List<AuditInfoDTO> findAllAuditInfoByUserId(Long userId);
}
