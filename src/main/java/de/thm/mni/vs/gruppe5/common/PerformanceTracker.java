package de.thm.mni.vs.gruppe5.common;

import de.thm.mni.vs.gruppe5.common.model.OrderItem;
import de.thm.mni.vs.gruppe5.common.model.Performance;

import java.io.Serializable;

/**
 * Helper class to track key performance indicators in a factory
 */
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

    /**
     * Reset all kpi and start a new tracking period
     */
    public void reset() {
        performance = new Performance();
    }

    /**
     * Track a received order
     */
    public void receivedOrder() {
        performance.incrementOrderCount();
    }

    /**
     * Track a finished / produced order item
     * @param item The produced order item
     * @param productionTime Time the production of the order item took
     */
    public void finishedOrderItem(OrderItem item, long productionTime) {
        // Calculate the production cost
        var productionCosts = productionTime * Config.PRODUCTION_COST_PER_SECOND;
        // Calculate the material cost of the order item's product by summing up all part costs
        var singleMaterialCosts = item.getProduct().getProductParts()
                .stream()
                .mapToDouble(p -> p.getPart().getCost() * p.getQuantity())
                .sum();
        // Calculate overall material costs for the order
        var materialCosts = singleMaterialCosts * item.getQuantity();

        // Calculate total order item costs
        var orderItemCosts = materialCosts + productionCosts;

        // Track count of produced products
        performance.increaseProducedProductsCount(item.getQuantity());
        // Track costs of produced products
        performance.increaseProducedProductsCost(orderItemCosts);
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
