package com.example.garchapplication.model.entity;

import com.example.garchapplication.model.enums.CalculationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

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

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "garch.calculation_status")
    private CalculationStatus status;

    @Column(name = "run_at", nullable = false)
    private Instant runAt;

    @ManyToOne
    @JoinColumn(name = "input_time_series_id")
    private TimeSeries inputTimeSeries;

    @OneToOne
    @JoinColumn(name = "result_time_series_id")
    private TimeSeries resultTimeSeries;

    @Column(name = "forecast", nullable = false)
    private int forecast;

    @Column(name = "start_variance", nullable = false)
    private double startVariance;

    @Column(name = "omega", nullable = false)
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

    public CalculationStatus getStatus() {
        return status;
    }

    public void setStatus(CalculationStatus status) {
        this.status = status;
    }

    public Instant getRunAt() {
        return runAt;
    }

    public void setRunAt(Instant runAt) {
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

    public int getForecast() {
        return forecast;
    }

    public void setForecast(int forecast) {
        this.forecast = forecast;
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
