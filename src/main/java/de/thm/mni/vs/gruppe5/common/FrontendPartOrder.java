package de.thm.mni.vs.gruppe5.common;

import java.util.HashMap;
import java.util.UUID;

public class FrontendPartOrder {
    private String id = UUID.randomUUID().toString();

    private HashMap<String, Integer> productIdWithQuantity;

    public FrontendPartOrder() {
    }

    public FrontendPartOrder(HashMap<String, Integer> productIdWithQuantity) {
        this.productIdWithQuantity = productIdWithQuantity;
    }

    public String getId() {
        return id;
    }

    public HashMap<String, Integer> getProductIdWithQuantity() {
        return productIdWithQuantity;
    }

    public void setProductIdWithQuantity(HashMap<String, Integer> productIdWithQuantity) {
        this.productIdWithQuantity = productIdWithQuantity;
    }

    @Override
    public String toString() {
        return "FrontendPartOrder{" +
                "id='" + id + '\'' +
                ", productIdWithQuantity=" + productIdWithQuantity +
                '}';
    }
}
