package de.thm.mni.vs.gruppe5.factory;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendPartOrder;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.Supplier;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Logic for ordering parts from a specified supplier
 */
public class PartProcurement {
    private Supplier supplier;

    public PartProcurement(Supplier supplier) {
        this.supplier = supplier;
    }

    /**
     * Get the parts for the specified order
     * For the purpose of simulation we will return the time the production needs to wait before
     * it can produce the product. We pretend that in this time the supplier will deliver the requested parts
     * @param order
     * @return time in seconds
     * @throws SocketTimeoutException if the supplier server isn't reachable
     * @throws IOException the supplier server send an invalid response
     * @throws RuntimeException the supplier can't provide the requested parts; this happens if you order from the wrong supplier
     */
    public int orderPartsFor(FridgeOrder order) throws SocketTimeoutException, IOException, RuntimeException {
        var parts = getPartsWithQuantity(order);
        return getWaitingTime(parts);
    }

    /**
     * Get all parts with the needed quantity for the specified order and supplier
     * @param order
     * @return Map with (partId -> quantity) for all parts
     */
    private Map<String, Integer> getPartsWithQuantity(FridgeOrder order) {
        Map<String, Integer> partsWithQuantity = new HashMap<>();

        for (var orderItem : order.getOrderItems()) {
            var orderItemQuantity = orderItem.getQuantity();
            // for all parts of a product: get the ones which are ordered from the given supplier
            // then store the id with the needed quantity for this product
            orderItem.getProduct().getProductParts().stream()
                    .filter(pp -> pp.getPart().getSupplier() == supplier)
                    .forEach(pp -> partsWithQuantity.put(pp.getPart().getId(), pp.getQuantity() * orderItemQuantity));
        }

        return partsWithQuantity;
    }

    /**
     * Request the supplier and get the waiting time from the response
     * @param productIdsWithQuantity the parts which should be ordered
     * @return time in seconds until the parts will be available
     * @throws IOException the supplier server send an invalid response
     * @throws RuntimeException the supplier can't provide the requested parts; this happens if you order from the wrong supplier
     */
    private int getWaitingTime(Map<String, Integer> productIdsWithQuantity) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .build();

        // Build the JSON-request
        var partOrder = new FrontendPartOrder(productIdsWithQuantity);
        String json = new Gson().toJson(partOrder);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        // Send request to supplier
        Request request = new Request.Builder()
                .url(Config.getPartsUrl(supplier))
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        if (response.code() == 200) {
            // request ok, we can get the waiting time from the response
            return Integer.parseInt(response.body().string());
        }

        throw new RuntimeException("Invalid response from the server: Supplier can't provide all requested parts");
    }
}
