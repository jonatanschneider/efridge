package de.thm.mni.vs.gruppe5.common.model;

import de.thm.mni.vs.gruppe5.common.Location;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Performance implements Serializable {
    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    private int orderCount;

    private int producedProductsCount;

    private float producedProductsCost;

    private Location location;

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Performance{" +
                "id='" + id + '\'' +
                ", orderCount=" + orderCount +
                ", producedProductsCount=" + producedProductsCount +
                ", producedProductsCost=" + producedProductsCost +
                ", location=" + location +
                '}';
    }
}
