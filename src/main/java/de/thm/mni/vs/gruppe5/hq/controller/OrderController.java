package de.thm.mni.vs.gruppe5.hq.controller;

import de.thm.mni.vs.gruppe5.common.FrontendOrder;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderItem;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import de.thm.mni.vs.gruppe5.common.model.Product;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import io.javalin.http.Context;

import javax.jms.JMSException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.Comparator;
import java.util.List;

public class OrderController {
    private final EntityManagerFactory emf;
    private final Publisher publisher;
    private final List<Product> products;

    public OrderController(EntityManagerFactory emf, Publisher publisher) {
        this.emf = emf;
        this.publisher = publisher;

        var em = emf.createEntityManager();
        Query query = em.createQuery("SELECT p FROM Product p");
        this.products = query.getResultList();
        this.products.sort(Comparator.comparing(Product::getId));
        em.close();
    }

    public void createOrder(Context ctx) throws JMSException {
        var frontendOrder = ctx.bodyAsClass(FrontendOrder.class);

        if (!frontendOrder.isValid()) {
            System.out.println("Discarding invalid order " + frontendOrder);
            ctx.status(400);
            return;
        }

        var order = buildFridgeOrder(frontendOrder);
        DatabaseUtility.persist(emf, order);
        System.out.println("Send order to factories: " + order.toString());
        publisher.publish(order);
        ctx.status(201);
    }

    private FridgeOrder buildFridgeOrder(FrontendOrder frontendOrder) {
        var order = new FridgeOrder();
        order.setCustomerId(frontendOrder.customerId);
        frontendOrder.getOrderProductIdsWithQuantity().entrySet().stream()
                .map(entry -> new OrderItem(products.get(entry.getKey() - 1), entry.getValue()))
                .forEach(order.getOrderItems()::add);
        order.setStatus(OrderStatus.RECEIVED);
        return order;
    }
}
