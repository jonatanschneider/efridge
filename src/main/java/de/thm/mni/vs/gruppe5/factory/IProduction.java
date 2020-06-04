package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import java.util.concurrent.Future;

public interface IProduction {
    Future<FridgeOrder> orderParts(FridgeOrder order);
    Future<FridgeOrder> produce(FridgeOrder order);
}
