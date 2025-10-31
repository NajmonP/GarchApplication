package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.GarchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarchModelRepository extends JpaRepository<GarchModel,Long> {
    List<GarchModel> findAllByConfigurationId(Long configurationId);
}
