package de.thm.mni.vs.gruppe5.factory;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendPartOrder;
import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import javax.persistence.EntityManagerFactory;
import de.thm.mni.vs.gruppe5.common.model.Supplier;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class Production implements IProduction {
    private PerformanceTracker performanceTracker;
    private EntityManagerFactory emf;

    public Production(EntityManagerFactory emf) {
        this.emf = emf;
        performanceTracker = PerformanceTracker.getInstance();
    }

    public CompletableFuture<FridgeOrder> orderParts(FridgeOrder order) {
        return CompletableFuture.supplyAsync(() -> {
            var mechanicParts = new HashMap<String, Integer>();
            var electricParts = new HashMap<String, Integer>();

            if (!order.hasInit()) {
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
                    var waitingTime = Math.max(orderParts(Supplier.CoolMechanics, mechanicParts), orderParts(Supplier.ElectroStuff, electricParts));
                    System.out.println("Waiting time for " + order + " ");
                    order.init(waitingTime);
                    DatabaseUtility.merge(emf, order);
                } catch (IOException ex) {
                    // If this happens, the supplier server send an invalid response, we can't do anything about that
                    ex.printStackTrace();
                } catch (RuntimeException ex) {
                    // This means our database is corrupt because either the part ids are incorrect or the ids are
                    // mapped to the wrong supplier
                    ex.printStackTrace();
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

        if (response.code() == 200) {
            return Integer.parseInt(response.body().string());
        }

        throw new RuntimeException("Invalid response from the server: Supplier can't provide all requested parts");
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
