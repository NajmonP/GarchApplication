package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.ModelBeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelBetaRepository extends JpaRepository<ModelBeta,Long> {
    public List<ModelBeta> findAllByGarchModelIdOrderByOrderNoAsc(Long modelId);

    public void deleteByGarchModelId(Long modelId);
}
