package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.RunVarianceWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunVarianceWeightRepository extends JpaRepository<RunVarianceWeight, Long> {
}
