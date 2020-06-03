package de.thm.mni.vs.gruppe5.common;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Map;

@Entity
public class Order {
    @Id
    private String id;

    private String customerId;

    //@ManyToMany(cascade = CascadeType.ALL)
    //private Map<String, Integer> product;

    private OrderStatus status;

    private boolean partsOrdered;

    public Order(String id, String customerId, /*Map<String, Integer> product,*/ OrderStatus status, boolean partsOrdered) {
        this.id = id;
        this.customerId = customerId;
        //this.product = product;
        this.status = status;
        this.partsOrdered = partsOrdered;
    }
}
