package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;

import java.util.concurrent.CompletableFuture;

/**
 * Interface description of a producer that is able to handle FridgeOrders.
 */
public interface IProduction {
    /**
     * Simulate ordering of parts
     *
     * @param order the order that needs to order parts
     * @return future that will contain the order after waiting time has elapsed
     */
    CompletableFuture<FridgeOrder> orderParts(FridgeOrder order);

    /**
     * Simulate production
     *
     * @param order the order that needs to be produced
     * @param factoryTimeFactor factory specific factor the waiting time is multiplied with
     * @return uture that will contain the order after waiting time has elapsed
     */
    CompletableFuture<FridgeOrder> produce(FridgeOrder order, float factoryTimeFactor);
}
