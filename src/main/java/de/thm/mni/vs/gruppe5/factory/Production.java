package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;

import java.util.Random;
import java.util.concurrent.CompletableFuture;


public class Production implements IProduction {
    private PerformanceTracker performanceTracker;

    public Production() {
        performanceTracker = PerformanceTracker.getInstance();
    }

    private void waitRandom(int maxSeconds) {
        var r = new Random();
        try {
            var i = r.nextInt(maxSeconds);
            System.out.println("Waiting " + i + " seconds");
            Thread.sleep(i * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                        waitRandom(10);
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
                try {
                    Thread.sleep(time * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                performanceTracker.finishedOrderItem(orderItem);
            });

            performanceTracker.finishedOrder(order);
            order.setStatus(OrderStatus.COMPLETED);
            return order;
        });
    }
}
