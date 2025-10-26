package com.example.garchapplication.repository;

import com.example.garchapplication.model.ModelShockWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelShockWeightRepository extends JpaRepository<ModelShockWeight,Long> {
}
