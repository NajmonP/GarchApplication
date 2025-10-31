package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSeriesRepository extends JpaRepository<TimeSeries,Long> {

    List<TimeSeries> getTimeSeriesByUser(User user);
}
