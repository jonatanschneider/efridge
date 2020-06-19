package de.thm.mni.vs.gruppe5.common;

import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderItem;

import java.io.Serializable;

public class PerformanceTracker implements Serializable {
    private int orderCount;
    private int producedProductsCount;
    private float producedProductsCost;

    private static PerformanceTracker instance;

    private PerformanceTracker() {
        reset();
    }

    public static PerformanceTracker getInstance() {
        if (instance == null) {
            instance = new PerformanceTracker();
        }
        return instance;
    }

    public void reset() {
        orderCount = 0;
        producedProductsCount = 0;
        producedProductsCost = 0;
    }

    public void receivedOrder(FridgeOrder order) {
        orderCount++;
    }

    public void finishedOrderItem(OrderItem item, long productionTime) {
        var productionCost = productionTime * Config.PRODUCTION_COST_PER_SECOND;
        var singleProductCost = item.getProduct().getProductParts()
                .stream()
                .mapToDouble(p -> p.getPart().getCost() * p.getQuantity())
                .sum();
        var productCost = singleProductCost * item.getQuantity();
        var orderItemCost = productCost + productionCost;

        producedProductsCount += item.getQuantity();
        producedProductsCost += orderItemCost;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public int getProducedProductsCount() {
        return producedProductsCount;
    }

    public float getProducedProductsCost() {
        return producedProductsCost;
    }

    @Override
    public String toString() {
        return "PerformanceTracker{" +
                "orderCount=" + orderCount +
                ", producedProducts=" + producedProductsCount +
                ", producedProductsCost=" + producedProductsCost +
                '}';
    }
}
