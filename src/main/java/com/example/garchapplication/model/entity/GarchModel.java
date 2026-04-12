package com.example.garchapplication.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "garch_model", schema = "garch")
public class GarchModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id", unique = true, nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "configuration_id", nullable = false)
    private Configuration configuration;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_variance", nullable = false)
    private double startVariance;

    @Column(name = "omega", nullable = false)
    private double constantVariance;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStartVariance() {
        return startVariance;
    }

    public void setStartVariance(double startVariance) {
        this.startVariance = startVariance;
    }

    public double getConstantVariance() {
        return constantVariance;
    }

    public void setConstantVariance(double constantVariance) {
        this.constantVariance = constantVariance;
    }
}
