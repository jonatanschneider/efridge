package de.thm.mni.vs.gruppe5.common;

import java.util.Map;

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
}
