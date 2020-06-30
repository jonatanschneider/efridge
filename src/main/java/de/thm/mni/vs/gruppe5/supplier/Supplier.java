package de.thm.mni.vs.gruppe5.supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.thm.mni.vs.gruppe5.common.FrontendPartOrder;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;

import java.util.Random;
import java.util.function.Predicate;

public abstract class Supplier {
    private Javalin server;

    public Supplier(int port) {
        server = Javalin.create().start(port);
        server.post("/parts", this::receivePartOrder);
        Gson gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);
    }

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

    Predicate<String> isAvailable = this::isAvailable;

    abstract boolean isAvailable(String partId);
}
