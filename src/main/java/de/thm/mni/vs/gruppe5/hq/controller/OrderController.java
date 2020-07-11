package de.thm.mni.vs.gruppe5.hq.controller;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendOrder;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.model.*;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import io.javalin.http.Context;

import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Comparator;
import java.util.List;

/**
 * HTTP controller to handle order related requests
 */
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

    /**
     * Handles post requests
     *
     * If the passed order is valid it gets stored in the db and passed to the factories
     * @param ctx request's context
     * @throws JMSException
     */
    public void createOrder(Context ctx) throws JMSException {
        var frontendOrder = ctx.bodyAsClass(FrontendOrder.class);

        if (!frontendOrder.isValid()) {
            System.out.println("Discarding invalid order " + frontendOrder);
            ctx.status(400);
            return;
        }

        var order = buildFridgeOrder(frontendOrder);
        DatabaseUtility.persist(emf, order);
        System.out.println("Send order to factories: " + order.toFormattedString());
        publisher.publish(order);
        ctx.status(201);
    }

    /**
     * Handles get requests for a specific order
     * @param ctx request's context
     */
    public void getOrder(Context ctx) {
        EntityManager em = emf.createEntityManager();
        ctx.json(em.find(FridgeOrder.class, ctx.pathParam("id")));
        em.close();
    }

    /**
     * Handles get requests for all orders
     * Optional parameter "customerId" to get all orders for a specific customer.
     * @param ctx request's context
     */
    public void getOrders(Context ctx) {
        EntityManager em = emf.createEntityManager();
        String customerId = ctx.queryParam(Config.CUSTOMER_ID_PARAM);
        TypedQuery<FridgeOrder> query;

        if (customerId != null) {
            query = em.createQuery("SELECT fo FROM FridgeOrder fo WHERE fo.customerId = :customerId", FridgeOrder.class);
            query.setParameter("customerId", customerId);
        } else {
            query = em.createQuery("SELECT fo FROM FridgeOrder fo", FridgeOrder.class);
        }
        ctx.json(query.getResultList());
        em.close();
    }

    /**
     * Helper function to build a fridge order from the request body.
     *
     * @param frontendOrder request object
     * @return built fridge order
     */
    private FridgeOrder buildFridgeOrder(FrontendOrder frontendOrder) {
        var order = new FridgeOrder();
        order.setCustomerId(frontendOrder.customerId);
        frontendOrder.getOrderProductIdsWithQuantity().entrySet().stream()
                .map(entry -> new OrderItem(products.get(entry.getKey()), entry.getValue()))
                .forEach(order.getOrderItems()::add);
        order.setStatus(OrderStatus.RECEIVED);
        return order;
    }
}
