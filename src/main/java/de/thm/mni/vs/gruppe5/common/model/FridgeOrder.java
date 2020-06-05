package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
public class FridgeOrder implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private String customerId;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems;

    private OrderStatus status;

    private boolean partsOrdered;

    public FridgeOrder() {
        this.orderItems = new HashSet<>();
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

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public boolean isPartsOrdered() {
        return partsOrdered;
    }

    public void setPartsOrdered(boolean partsOrdered) {
        this.partsOrdered = partsOrdered;
    }

    @Override
    public String toString() {
        return "FridgeOrder{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", orderItems=" + orderItems +
                ", status=" + status +
                ", partsOrdered=" + partsOrdered +
                '}';
    }
}
