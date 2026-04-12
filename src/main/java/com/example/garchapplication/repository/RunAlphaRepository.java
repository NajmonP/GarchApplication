package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.RunAlpha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunAlphaRepository extends JpaRepository<RunAlpha, Long> {
    public List<RunAlpha> findAllByCalculationIdOrderByOrderNoAsc(Long calculationId);
}
