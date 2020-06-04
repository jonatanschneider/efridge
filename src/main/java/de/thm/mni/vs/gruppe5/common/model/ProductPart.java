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

    public String getId() {
        return id;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProductPart{" +
                "id='" + id + '\'' +
                ", part=" + part +
                ", quantity=" + quantity +
                '}';
    }
}
