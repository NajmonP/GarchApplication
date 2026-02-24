package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.TimeSeries;
import com.example.garchapplication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSeriesRepository extends JpaRepository<TimeSeries,Long> {

    @Query("select t.name from TimeSeries t where t.id = :id")
    Optional<String> findNameById(@Param("id") long id);

    List<TimeSeries> getTimeSeriesByUser(User user);
}
