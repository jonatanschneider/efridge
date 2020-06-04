package de.thm.mni.vs.gruppe5.common;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Part {
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
