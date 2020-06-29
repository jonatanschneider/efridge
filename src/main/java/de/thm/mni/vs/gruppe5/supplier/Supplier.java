package de.thm.mni.vs.gruppe5.supplier;

import de.thm.mni.vs.gruppe5.common.FrontendPartOrder;
import de.thm.mni.vs.gruppe5.common.model.Part;
import de.thm.mni.vs.gruppe5.util.TimeHelper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.function.Predicate;

public abstract class Supplier {
    private Javalin server;

    public Supplier(int port) {
        server = Javalin.create().start(port);
        server.post("/parts", this::receivePartOrder);
    }

    public void receivePartOrder(Context ctx) {
        var order = ctx.bodyAsClass(FrontendPartOrder.class);
        if (!order.getProductIdWithQuantity().keySet().stream().allMatch(isAvailable)) {
            System.out.println("Some parts are not available from this supplier, cancelling");
            ctx.status(400);
            return;
        }
        System.out.println("We received a order for " + order);
        System.out.println("Processing order...");
        TimeHelper.waitRandom(10);
        System.out.println("Finished processing, sending 200 OK");
        ctx.status(200);
    }

    Predicate<String> isAvailable = this::isAvailable;

    abstract boolean isAvailable(String partId);
}
