package com.example.garchapplication.repository;

import com.example.garchapplication.model.GarchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarchModelRepository extends JpaRepository<GarchModel,Long> {
}
