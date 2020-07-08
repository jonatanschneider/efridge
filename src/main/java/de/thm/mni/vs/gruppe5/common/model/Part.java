package de.thm.mni.vs.gruppe5.common.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Represents a specific part which can be ordered from one of our supplier
 */
@Entity
public class Part implements Serializable {
    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    private double cost;

    private Supplier supplier;

    public Part() {
    }

    public Part(double cost, Supplier supplier) {
        this.cost = cost;
        this.supplier = supplier;
    }

    public String getId() {
        return id;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public String toString() {
        return "Part{" +
                "id='" + id + '\'' +
                ", cost=" + cost +
                ", supplier=" + supplier +
                '}';
    }
}
