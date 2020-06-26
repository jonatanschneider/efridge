package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Entity
public class FridgeOrder implements Serializable, Completable {
    @Id
    private String id = UUID.randomUUID().toString();

    private String customerId;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems;

    private OrderStatus status;

    private boolean partsOrdered;

    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;

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

    @Override
    public void init(int seconds) {
        completedAt = new Date(System.currentTimeMillis() + seconds * 1000);
    }

    @Override
    public void initRandom(int seconds) {
        var r = new Random();
        var waitTime = orderItems.stream()
                .flatMap(orderItem -> orderItem.getProduct().getProductParts().stream())
                .map(productPart -> productPart.getPart().getSupplier())
                .distinct()
                .mapToInt(x -> r.nextInt(seconds))
                .sum();
        init(waitTime);
        System.out.println("Set wait time for FridgeOrder " + id + ": " + waitTime + " seconds");
    }

    @Override
    public boolean hasInit() {
        return completedAt != null;
    }

    @Override
    public void complete() throws InterruptedException {
        var suppliers = orderItems.stream()
                .flatMap(orderItem -> orderItem.getProduct().getProductParts().stream())
                .map(productPart -> productPart.getPart().getSupplier())
                .distinct()
                .map(Enum::toString)
                .toArray(String[]::new);
        System.out.println("Ordering from " + String.join(", ", suppliers));
        while (completedAt.after(new Date(System.currentTimeMillis())))
            Thread.sleep(1000);
    }
}
