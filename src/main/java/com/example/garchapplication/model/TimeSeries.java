package com.example.garchapplication.model;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "time_series", schema = "garch")
public class TimeSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_series_id", unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name",  nullable = false)
    private String name;

    @Column(name = "mean")
    private double mean;

    @Column(name = "median")
    private double median;

    @Column(name = "variance")
    private double variance;

    @Column(name = "skewness")
    private double skewness;

    @Column(name = "kurtosis")
    private double kurtosis;

    @Column(name = "created_at", nullable = false)
    private Date created;

    @Column(name = "visibility", nullable = false)
    private String visibility;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(double skewness) {
        this.skewness = skewness;
    }

    public double getKurtosis() {
        return kurtosis;
    }

    public void setKurtosis(double kurtosis) {
        this.kurtosis = kurtosis;
    }

    public Date getCreatedAt() {
        return created;
    }

    public void setCreatedAt(Date created) {
        this.created = created;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
