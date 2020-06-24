package de.thm.mni.vs.gruppe5.common;

import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderItem;
import de.thm.mni.vs.gruppe5.common.model.Performance;

import java.io.Serializable;

public class PerformanceTracker implements Serializable {
    private Performance performance;

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
        performance = new Performance();
    }

    public void receivedOrder() {
        performance.incrementOrderCount();
    }

    public void finishedOrderItem(OrderItem item, long productionTime) {
        var productionCost = productionTime * Config.PRODUCTION_COST_PER_SECOND;
        var singleProductCost = item.getProduct().getProductParts()
                .stream()
                .mapToDouble(p -> p.getPart().getCost() * p.getQuantity())
                .sum();
        var productCost = singleProductCost * item.getQuantity();
        var orderItemCost = productCost + productionCost;

        performance.increaseProducedProductsCount(item.getQuantity());
        performance.increaseProducedProductsCost(orderItemCost);
    }

    public Performance getPerformance() {
        return this.performance;
    }

    @Override
    public String toString() {
        return "PerformanceTracker{" +
                "performance=" + performance +
                '}';
    }
}
