package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Part implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private double cost;

    private Supplier supplier;

    public Part() {
    }

    public Part(double cost, Supplier supplier) {
        this.cost = cost;
        this.supplier = supplier;
    }
}
