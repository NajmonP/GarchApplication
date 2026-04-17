package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.GarchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarchModelRepository extends JpaRepository<GarchModel,Long> {

    @Query("select g.name from GarchModel g where g.id = :id")
    Optional<String> findNameById(@Param("id") long id);

    List<GarchModel> findAllByConfigurationId(Long configurationId);

    boolean existsByIdAndConfigurationUserId(Long id, Long userId);
}
