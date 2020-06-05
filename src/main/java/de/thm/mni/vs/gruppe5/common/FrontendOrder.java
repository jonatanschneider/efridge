package de.thm.mni.vs.gruppe5.common;

import java.util.Map;
import java.util.function.Predicate;

public class FrontendOrder {
    public String customerId;

    public Map<Integer, Integer> orderProductIdsWithQuantity;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Map<Integer, Integer> getOrderProductIdsWithQuantity() {
        return orderProductIdsWithQuantity;
    }

    public void setOrderProductIdsWithQuantity(Map<Integer, Integer> orderProductIdsWithQuantity) {
        this.orderProductIdsWithQuantity = orderProductIdsWithQuantity;
    }

    public boolean isValid() {
        return getOrderProductIdsWithQuantity().entrySet().stream()
                .anyMatch(entrySet ->
                        entrySet.getKey() >= 0 &&
                        entrySet.getKey() < 5 &&
                        entrySet.getValue() > 0);
    }

    @Override
    public String toString() {
        return "FrontendOrder{" +
                "customerId='" + customerId + '\'' +
                ", orderProductIdsWithQuantity=" + orderProductIdsWithQuantity +
                '}';
    }
}
