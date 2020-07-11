package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Represents an order of a customer with (multiple) items
 */
@Entity
public class FridgeOrder implements Serializable, Completable {
    @Id
    private String id = UUID.randomUUID().toString();

    private String customerId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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

    /**
     * Returns a formatted string representation destined for output. In contrast to toString, not all attributes are
     * necessarily output here.
     * @return a string representation of the object
     */
    public String toFormattedString() {
        return "FridgeOrder{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * Initialise waiting time by storing a Date in the future
     *
     * @param seconds waiting time in seconds
     */
    @Override
    public void init(int seconds) {
        completedAt = new Date(System.currentTimeMillis() + seconds * 1000);
    }

    /**
     * Initialise waiting time by storing a Date in the future. Pick a random waiting time from 0 to parameter for each supplier involved in the order and add up.
     *
     * @param seconds maximum waiting time in seconds
     */
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

    /**
     * @return whether or not a completion date has been set
     */
    @Override
    public boolean hasInit() {
        return completedAt != null;
    }

    /**
     * Prints suppliers involved in the order, then uses Thread.sleep to wait until completion date is in the past.
     *
     * @throws InterruptedException in case of manual interruption
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FridgeOrder that = (FridgeOrder) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
