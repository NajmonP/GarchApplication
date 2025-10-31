package com.example.garchapplication.repository;

import com.example.garchapplication.model.entity.Configuration;
import com.example.garchapplication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    List<Configuration> getConfigurationsByUser(User user);
}
