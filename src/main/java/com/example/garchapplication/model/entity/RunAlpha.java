package com.example.garchapplication.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "run_alpha", schema = "garch")
public class RunAlpha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_alpha_id", unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "calculation_id", nullable = false)
    private Calculation calculation;

    @Column(name = "order_no ", nullable = false)
    private int orderNo;

    @Column(name = "value", nullable = false)
    private double value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Calculation getCalculation() {
        return calculation;
    }

    public void setCalculation(Calculation calculation) {
        this.calculation = calculation;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
