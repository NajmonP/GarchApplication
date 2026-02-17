package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.ModelShockWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelShockWeightRepository extends JpaRepository<ModelShockWeight,Long> {
    public List<ModelShockWeight> findAllByGarchModelIdOrderByOrderNoAsc(Long modelId);

    public void deleteByGarchModelId(Long modelId);
}
