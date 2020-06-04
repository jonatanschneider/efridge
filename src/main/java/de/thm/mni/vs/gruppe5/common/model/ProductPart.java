package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class ProductPart implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Part part;

    private int quantity;

    public ProductPart() {
    }

    public ProductPart(Part part, int quantity) {
        this.part = part;
        this.quantity = quantity;
    }
}
