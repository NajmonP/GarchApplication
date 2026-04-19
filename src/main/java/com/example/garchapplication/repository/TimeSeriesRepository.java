package com.example.garchapplication.repository;

import com.example.garchapplication.model.dto.AuditInfoDTO;
import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSeriesRepository extends JpaRepository<TimeSeries, Long> {

    @Query("select t.name from TimeSeries t where t.id = :id")
    Optional<String> findNameById(@Param("id") long id);

    @Query("select t from TimeSeries t where t.visibility = 'Public'")
    Page<TimeSeries> findPublicTimeSeries(Pageable pageable);

    List<TimeSeries> getTimeSeriesByUser(User user);

    List<TimeSeries> getTimeSeriesByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndVisibility(Long id, String visibility);

    @Query("""
                select new com.example.garchapplication.model.dto.AuditInfoDTO(
                com.example.garchapplication.model.enums.EntityType.TIME_SERIES,
                t.id,
                t.name)
                from TimeSeries t
                where t.user.id = :userId
            """)
    List<AuditInfoDTO> findAllAuditInfoByUserId(Long userId);
}
