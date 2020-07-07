package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import de.thm.mni.vs.gruppe5.common.model.Supplier;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public class Production implements IProduction {
    private final PerformanceTracker performanceTracker;
    private final EntityManagerFactory emf;

    public Production(EntityManagerFactory emf) {
        this.emf = emf;
        performanceTracker = PerformanceTracker.getInstance();
    }

    public CompletableFuture<FridgeOrder> orderParts(FridgeOrder order) {
        return CompletableFuture.supplyAsync(() -> {

            if (!order.hasInit()) {
                try {
                    // Check which supplier takes the longest and wait for that time
                    // We don't need to wait for both, because the ordering and delivering would happen simultaneously,
                    var waitingTime = Math.max(
                            new PartProcurement(Supplier.CoolMechanics).orderPartsFor(order),
                            new PartProcurement(Supplier.ElectroStuff).orderPartsFor(order));

                    System.out.println("Waiting time for " + order.getId() + " is " + waitingTime + " seconds");
                    order.init(waitingTime);

                    // Persist the information that we successfully ordered the parts
                    DatabaseUtility.merge(emf, order);
                } catch (IOException ex) {
                    // If this happens, the supplier server send an invalid response, we can't do anything about that
                    return order;
                } catch (RuntimeException ex) {
                    // This means our database is corrupt because either the part ids are incorrect or the ids are
                    // mapped to the wrong supplier
                    return order;
                }
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
            if (!order.isPartsOrdered()) {
                // parts weren't ordered, so we can't produce
                return order;
            }
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
