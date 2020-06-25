package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.util.TimeHelper;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;

import java.util.concurrent.CompletableFuture;


public class Production implements IProduction {
    private PerformanceTracker performanceTracker;

    public Production() {
        performanceTracker = PerformanceTracker.getInstance();
    }

    @Override
    public CompletableFuture<FridgeOrder> orderParts(FridgeOrder order) {
        return CompletableFuture.supplyAsync(() -> {

            /* Wait for all suppliers where we need to order items for this FridgeOrder */
            order.getOrderItems().stream()
                    .flatMap(orderItem -> orderItem.getProduct().getProductParts().stream())
                    .map(productPart -> productPart.getPart().getSupplier())
                    .distinct()
                    .forEach(x -> {
                        System.out.println("Ordering from " + x.name());
                        TimeHelper.waitRandom(10);
                    });

            order.setPartsOrdered(true);
            return order;
        });
    }

    @Override
    public CompletableFuture<FridgeOrder> produce(FridgeOrder order, float factoryTimeFactor) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Producing order " + order.getId());

            order.getOrderItems().forEach(orderItem -> {
                var product = orderItem.getProduct();
                var time = (long) (product.getProductionTime() * orderItem.getQuantity() * factoryTimeFactor);
                System.out.println("Producing '" + product.getName() + "' (" + orderItem.getQuantity() + "x): " + time + " seconds");
                orderItem.waitCompletition(time);
                performanceTracker.finishedOrderItem(orderItem, time);
            });

            order.setStatus(OrderStatus.COMPLETED);
            return order;
        });
    }
}
