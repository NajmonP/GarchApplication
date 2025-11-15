package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.TimeSeriesValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSeriesValueRepository extends JpaRepository<TimeSeriesValue, Long> {
    public List<TimeSeriesValue> findAllByTimeSeriesIdOrderByOrderNo(Long timeSeriesId);
}
