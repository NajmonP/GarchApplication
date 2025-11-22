package com.example.garchapplication.model.entity;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "calculation", schema = "garch")
public class Calculation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calculation_id", unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private GarchModel garchModel;

    @Column(name = "run_at", nullable = false)
    private Date runAt;

    @ManyToOne
    @JoinColumn(name = "input_time_series_id")
    private TimeSeries inputTimeSeries;

    @OneToOne
    @JoinColumn(name = "result_time_series_id", nullable = false, unique = true)
    private TimeSeries resultTimeSeries;

    @Column(name = "start_variance", nullable = false)
    private double startVariance;

    @Column(name = "constant_variance", nullable = false)
    private double constantVariance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GarchModel getGarchModel() {
        return garchModel;
    }

    public void setGarchModel(GarchModel garchModel) {
        this.garchModel = garchModel;
    }

    public Date getRunAt() {
        return runAt;
    }

    public void setRunAt(Date runAt) {
        this.runAt = runAt;
    }

    public TimeSeries getInputTimeSeries() {
        return inputTimeSeries;
    }

    public void setInputTimeSeries(TimeSeries inputTimeSeries) {
        this.inputTimeSeries = inputTimeSeries;
    }

    public TimeSeries getResultTimeSeries() {
        return resultTimeSeries;
    }

    public void setResultTimeSeries(TimeSeries resultTimeSeries) {
        this.resultTimeSeries = resultTimeSeries;
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
