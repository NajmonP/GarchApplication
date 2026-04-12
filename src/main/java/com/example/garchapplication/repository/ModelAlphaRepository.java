package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.ModelAlpha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelAlphaRepository extends JpaRepository<ModelAlpha, Long> {
    public List<ModelAlpha> findAllByGarchModelIdOrderByOrderNoAsc(Long modelId);

    public void deleteByGarchModelId(Long modelId);
}
