package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.util.Set;

@Entity
public class FridgeOrder {
    @Id
    @GeneratedValue
    private String id;

    private String customerId;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems;

    private OrderStatus status;

    private boolean partsOrdered;

    public FridgeOrder() {

    }

    public FridgeOrder(String customerId, Set<OrderItem> orderItems, OrderStatus status, boolean partsOrdered) {
        this.customerId = customerId;
        this.orderItems = orderItems;
        this.status = status;
        this.partsOrdered = partsOrdered;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isPartsOrdered() {
        return partsOrdered;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setPartsOrdered(boolean partsOrdered) {
        this.partsOrdered = partsOrdered;
    }
}
