package com.example.garchapplication.repository;

import com.example.garchapplication.model.TimeSeriesValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSeriesValueRepository extends JpaRepository<TimeSeriesValue, Long> {
}
