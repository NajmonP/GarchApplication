package com.example.garchapplication.repository;

import com.example.garchapplication.model.ModelVarianceWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelVarianceWeightRepository extends JpaRepository<ModelVarianceWeight, Long> {
}
