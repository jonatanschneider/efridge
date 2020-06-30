package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;

import javax.persistence.EntityManagerFactory;
import java.util.concurrent.CompletableFuture;


public class Production implements IProduction {
    private PerformanceTracker performanceTracker;
    private EntityManagerFactory emf;

    public Production(EntityManagerFactory emf) {
        this.emf = emf;
        performanceTracker = PerformanceTracker.getInstance();
    }

    @Override
    public CompletableFuture<FridgeOrder> orderParts(FridgeOrder order) {
        return CompletableFuture.supplyAsync(() -> {
            if (!order.hasInit()) {
                order.initRandom(10);
                DatabaseUtility.merge(emf, order);
            }
            try {
                order.complete();
            } catch (InterruptedException e) {
                System.out.println("Manually interrupting waiting time");
            }

            order.setPartsOrdered(true);
            DatabaseUtility.merge(emf, order);
            return order;
        });
    }

    @Override
    public CompletableFuture<FridgeOrder> produce(FridgeOrder order, float factoryTimeFactor) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Producing order " + order.getId());

            order.getOrderItems().forEach(orderItem -> {
                var product = orderItem.getProduct();
                var time = Math.round(product.getProductionTime() * orderItem.getQuantity() * factoryTimeFactor);
                System.out.println("Producing '" + product.getName() + "' (" + orderItem.getQuantity() + "x): " + time + " seconds");
                if (!orderItem.hasInit()) {
                    orderItem.init(time);
                    DatabaseUtility.merge(emf, orderItem);
                }
                try {
                    orderItem.complete();
                } catch (InterruptedException e) {
                    System.out.println("Manually interrupting waiting time");
                }
                performanceTracker.finishedOrderItem(orderItem, time);
            });

            order.setStatus(OrderStatus.COMPLETED);
            return order;
        });
    }
}
