package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;

@Entity
public class OrderItem {
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
