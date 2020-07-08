package de.thm.mni.vs.gruppe5.common;

import java.util.Map;
import java.util.UUID;

/**
 * Contains information about the order of a single part.
 * Used as common model for communication from any frontend to our backend (the HQ)
 */
public class FrontendPartOrder {
    private String id = UUID.randomUUID().toString();

    private Map<String, Integer> productIdWithQuantity;

    public FrontendPartOrder() {
    }

    public FrontendPartOrder(Map<String, Integer> productIdWithQuantity) {
        this.productIdWithQuantity = productIdWithQuantity;
    }

    public String getId() {
        return id;
    }

    public Map<String, Integer> getProductIdWithQuantity() {
        return productIdWithQuantity;
    }

    public void setProductIdWithQuantity(Map<String, Integer> productIdWithQuantity) {
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
