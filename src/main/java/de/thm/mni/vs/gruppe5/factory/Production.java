package de.thm.mni.vs.gruppe5.factory;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendPartOrder;
import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.common.model.Supplier;
import de.thm.mni.vs.gruppe5.util.TimeHelper;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class Production implements IProduction {
    private PerformanceTracker performanceTracker;

    public Production() {
        performanceTracker = PerformanceTracker.getInstance();
    }

    public CompletableFuture<FridgeOrder> orderParts(FridgeOrder order) {
        return CompletableFuture.supplyAsync(() -> {
            int mechanicPartsWaitingTime = 0;
            int electricPartsWaitingTime = 0;
            var mechanicParts = new HashMap<String, Integer>();
            var electricParts = new HashMap<String, Integer>();

            for (var orderItem : order.getOrderItems()) {
                var quantity = orderItem.getQuantity();
                for (var productPart : orderItem.getProduct().getProductParts()) {
                    if (productPart.getPart().getSupplier() == Supplier.CoolMechanics) {
                        mechanicParts.put(productPart.getPart().getId(), productPart.getQuantity() * quantity);
                    } else {
                        electricParts.put(productPart.getPart().getId(), productPart.getQuantity() * quantity);
                    }
                }
            }

            try {
                mechanicPartsWaitingTime = orderParts(Supplier.CoolMechanics, mechanicParts);
                electricPartsWaitingTime = orderParts(Supplier.ElectroStuff, electricParts);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            TimeHelper.waitTime(Math.max(mechanicPartsWaitingTime, electricPartsWaitingTime));

            order.setPartsOrdered(true);
            return order;
        });
    }

    private int orderParts(Supplier supplier, Map<String, Integer> productIdsWithQuantity) throws IOException {
        OkHttpClient client = new OkHttpClient();
        var partOrder = new FrontendPartOrder(productIdsWithQuantity);
        String json = new Gson().toJson(partOrder);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(Config.getPartsUrl(supplier))
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return Integer.parseInt(response.body().string());
    }

    @Override
    public CompletableFuture<FridgeOrder> produce(FridgeOrder order, float factoryTimeFactor) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Producing order " + order.getId());

            order.getOrderItems().forEach(orderItem -> {
                var product = orderItem.getProduct();
                var time = (long) (product.getProductionTime() * orderItem.getQuantity() * factoryTimeFactor);
                System.out.println("Producing '" + product.getName() + "' (" + orderItem.getQuantity() + "x): " + time + " seconds");
                TimeHelper.waitTime(time);

                performanceTracker.finishedOrderItem(orderItem, time);
            });

            order.setStatus(OrderStatus.COMPLETED);
            return order;
        });
    }
}
