package com.example.garchapplication.model;

import jakarta.persistence.*;

@Entity
@Table(name = "time_series_value", schema = "garch")
public class TimeSeriesValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id", unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "time_series_id", nullable = false)
    private TimeSeries timeSeries;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "order_no", nullable = false)
    private int orderNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }
}
