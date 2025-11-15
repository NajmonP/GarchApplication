package com.example.garchapplication.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "model_shock_weight", schema = "garch")
public class ModelShockWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_shock_weight_id", unique = true, nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private GarchModel garchModel;

    @Column(name = "order_no ", nullable = false)
    private int orderNo;

    @Column(name = "value", nullable = false)
    private double value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GarchModel getGarchModel() {
        return garchModel;
    }

    public void setGarchModel(GarchModel garchModel) {
        this.garchModel = garchModel;
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
