package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Performance implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private int orderCount;

    private int producedProductsCount;

    private float producedProductsCost;

    public Performance() {

    }

    public Performance(int orderCount, int producedProductsCount, float producedProductsCost) {
        this.orderCount = orderCount;
        this.producedProductsCount = producedProductsCount;
        this.producedProductsCost = producedProductsCost;
    }

    public String getId() {
        return id;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void incrementOrderCount() {
        this.orderCount++;
    }

    public int getProducedProductsCount() {
        return producedProductsCount;
    }

    public void increaseProducedProductsCount(int producedProductsCount) {
        this.producedProductsCount += producedProductsCount;
    }

    public float getProducedProductsCost() {
        return producedProductsCost;
    }

    public void increaseProducedProductsCost(double producedProductsCost) {
        this.producedProductsCost += producedProductsCost;
    }

    @Override
    public String toString() {
        return "Performance{" +
                "id='" + id + '\'' +
                ", orderCount=" + orderCount +
                ", producedProductsCount=" + producedProductsCount +
                ", producedProductsCost=" + producedProductsCost +
                '}';
    }
}
