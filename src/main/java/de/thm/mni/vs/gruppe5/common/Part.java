package de.thm.mni.vs.gruppe5.common;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Part {
    @Id
    public String id;

    public double cost;

    public Supplier supplier;
}
