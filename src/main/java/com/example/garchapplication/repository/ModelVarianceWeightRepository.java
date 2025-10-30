package com.example.garchapplication.repository;

import com.example.garchapplication.model.ModelShockWeight;
import com.example.garchapplication.model.ModelVarianceWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelVarianceWeightRepository extends JpaRepository<ModelVarianceWeight, Long> {
    public List<ModelVarianceWeight> findAllByGarchModelId(Long modelId);
}
