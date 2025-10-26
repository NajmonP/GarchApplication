package com.example.garchapplication.repository;

import com.example.garchapplication.model.Configuration;
import com.example.garchapplication.model.GarchModel;
import com.example.garchapplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    List<Configuration> getConfigurationsByUser(User user);
}
