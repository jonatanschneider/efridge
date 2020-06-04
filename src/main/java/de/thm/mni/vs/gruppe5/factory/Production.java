package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderItem;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Production implements IProduction {
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
            System.out.println("Ordering");
            waitRandom(10);
            return order;
        });
    }

    @Override
    public CompletableFuture<FridgeOrder> produce(FridgeOrder order) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Producing");
            waitRandom(10);
            return order;
        });
    }
}
