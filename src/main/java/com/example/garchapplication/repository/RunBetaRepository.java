package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.RunBeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunBetaRepository extends JpaRepository<RunBeta, Long> {
    public List<RunBeta> findAllByCalculationIdOrderByOrderNoAsc(Long calculationId);
}
