package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class OrderItem implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    private Product product;

    private int quantity;


    public OrderItem() {
    }

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}
