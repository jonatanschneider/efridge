package de.thm.mni.vs.gruppe5.supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.thm.mni.vs.gruppe5.common.FrontendPartOrder;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;

import java.util.Random;
import java.util.function.Predicate;

/**
 * Abstract class for a supplier
 * Contains logic for receiving a REST request
 */
public abstract class Supplier {
    private Javalin server;

    /**
     * Create a new instance of a specific supplier
     * Server listens to POST /parts at the port
     * @param port port the server should listen on
     */
    public Supplier(int port) {
        server = Javalin.create().start(port);
        server.post("/parts", this::receivePartOrder);
        Gson gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);
    }

    /**
     * Receive a POST with a FrontendPartOrder
     * Response will contain time in seconds until the parts will be delivered (this is for simulation purposes)
     * @param ctx javalin context
     */
    public void receivePartOrder(Context ctx) {
        var order = ctx.bodyAsClass(FrontendPartOrder.class);
        if (!order.getProductIdWithQuantity().keySet().stream().allMatch(isAvailable)) {
            System.out.println("Some parts are not available from this supplier, cancelling");
            ctx.status(400);
            ctx.result("There are some parts that are not available from this supplier, please check your part ids");
            return;
        }
        System.out.println("We received a order for " + order);

        var r = new Random();
        var seconds = r.nextInt(10);

        System.out.println("Received order, will be available in " + seconds +  " seconds");

        ctx.result("" + seconds);
        ctx.status(200);
    }

    /**
     * Check whether this supplier has a part with the given id
     * @param partId
     * @return true if the part can be ordered from this supplier
     */
    abstract boolean isAvailable(String partId);

    Predicate<String> isAvailable = this::isAvailable;
}
