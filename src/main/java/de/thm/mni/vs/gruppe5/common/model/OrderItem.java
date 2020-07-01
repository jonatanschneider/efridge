package de.thm.mni.vs.gruppe5.common.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

@Entity
public class OrderItem implements Serializable, Completable {
    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    @OneToOne
    private Product product;

    private int quantity;

    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;


    public OrderItem() {
    }

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id='" + id + '\'' +
                ", product=" + product +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public void init(int seconds) {
        completedAt = new Date(System.currentTimeMillis() + seconds * 1000);
    }

    @Override
    public void initRandom(int seconds) {
        init(new Random().nextInt(seconds) + 1);
    }

    @Override
    public boolean hasInit() {
        return completedAt != null;
    }

    @Override
    public void complete() throws InterruptedException {
        while (completedAt.after(new Date(System.currentTimeMillis())))
            Thread.sleep(1000);
    }
}
