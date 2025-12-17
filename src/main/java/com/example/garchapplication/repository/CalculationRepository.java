package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.Calculation;
import com.example.garchapplication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationRepository extends JpaRepository<Calculation, Integer> {
    List<Calculation> getCalculationsByUser(User user);
}
