package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.RunShockWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunShockWeightRepository extends JpaRepository<RunShockWeight, Long> {
}
