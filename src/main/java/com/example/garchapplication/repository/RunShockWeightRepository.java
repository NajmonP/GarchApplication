package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.RunShockWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunShockWeightRepository extends JpaRepository<RunShockWeight, Long> {
    public List<RunShockWeight> findAllByCalculationIdOrderByOrderNoAsc(Long calculationId);
}
