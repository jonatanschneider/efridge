package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import java.util.concurrent.CompletableFuture;

public interface IProduction {
    CompletableFuture<FridgeOrder> produce(FridgeOrder order);
}
