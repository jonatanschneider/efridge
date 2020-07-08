package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import de.thm.mni.vs.gruppe5.common.model.Part;
import de.thm.mni.vs.gruppe5.factory.report.ReportTask;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManagerFactory;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a factory which is responsible for producing products
 */
public class Factory {
    private final Location location;
    private final Publisher finishedOrderPublisher;
    private final Publisher reportPublisher;
    private final Subscriber orderSubscriber;
    private final Subscriber updatePartCostSubscriber;
    private final IProduction production;
    private final float productionTimeFactor;
    private final int maxCapacity;
    private final List<FridgeOrder> currentOrders;
    private EntityManagerFactory emf;

    /**
     * Start a new factory, needs three parameters:
     * 0 - location (can be USA or CHINA)
     * 1 - production time factor - a float which defines how fast this factory works (1 = default, 0.5 = 2x speed)
     * 2 - max capacity - defines how many orders this factory can take at once
     * @param args 0 - location 1 - productionTimeFactor 2 - maxCapacity
     */
    public static void main(String[] args) {
        Location location;
        float productionTimeFactor = 0;
        int maxCapacity = 0;

        if (args.length != 3) {
            System.err.println("Invalid number of program arguments");
            System.err.println("Usage: Factory <LOCATION> <productionTimeFactor> <maxCapacity>");
            return;
        }

        switch (args[0]) {
            case "USA" -> location = Location.USA;
            case "CHINA" -> location = Location.CHINA;
            default -> {
                System.err.println("Invalid location " + args[0]);
                System.err.println("Valid locations are 'USA', 'CHINA'");
                System.err.println("Usage: Factory <LOCATION> <productionTimeFactor> <maxCapacity>");
                return;
            }
        }

        try {
            productionTimeFactor = Float.parseFloat(args[1]);
            maxCapacity = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.err.println("Invalid production time factor or max capacity");
            System.err.println("Usage: Factory <LOCATION> <productionTimeFactor> <maxCapacity>");
            return;
        }

        try {
            var factory = new Factory(location, productionTimeFactor, maxCapacity);
            // On exit close all opened resources
            Runtime.getRuntime().addShutdownHook(factory.closeResources());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a new factory
     * @param location location of the factory
     * @param productionTimeFactor defines how fast this factory works (1 = default, 0.5 = 2x speed)
     * @param maxCapacity defines how many orders this factory can take at once
     * @throws JMSException
     */
    public Factory(Location location, float productionTimeFactor, int maxCapacity) throws JMSException {
        this.location = location;
        this.productionTimeFactor = productionTimeFactor;
        this.maxCapacity = maxCapacity;
        this.currentOrders = Collections.synchronizedList(new ArrayList<>(maxCapacity));
        this.emf = DatabaseUtility.getEntityManager(this.location);

        // Initialize publisher and subscriber
        this.orderSubscriber = new Subscriber(Config.ORDER_QUEUE, processOrderListener);
        this.finishedOrderPublisher = new Publisher(Config.FINISHED_ORDER_QUEUE);
        this.production = new Production(emf);
        this.reportPublisher = new Publisher(Config.REPORT_QUEUE);

        // Select correct queue for this location for updating the part costs
        if (location == Location.USA) {
            this.updatePartCostSubscriber = new Subscriber(Config.UPDATE_PARTS_COST_QUEUE_US, updatePartCosts);
        } else {
            this.updatePartCostSubscriber = new Subscriber(Config.UPDATE_PARTS_COST_QUEUE_CN, updatePartCosts);
        }

        // Initialize and start report task
        var reportTask = new ReportTask(location, reportPublisher);
        new Timer().scheduleAtFixedRate(reportTask, 0, reportTask.getPeriod());

        System.out.println("Factory - " + location.name()
                + " - productionTimeFactor: " + productionTimeFactor
                + " - maxCapacity: " + maxCapacity);

        // Re-initialize previously interrupted orders
        var em = emf.createEntityManager();
        var query = em.createQuery("SELECT f FROM FridgeOrder f");
        ((List<FridgeOrder>) query.getResultList())
                .stream()
                .filter(f -> f.getStatus() != OrderStatus.COMPLETED)
                .forEach(this::processOrder);
        em.close();
    }

    private final MessageListener processOrderListener = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var order = (FridgeOrder) objectMessage.getObject();
            processOrder(order);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    };

    /**
     * Process a incoming order from the hq
     * @param order the order which should be produced
     * @throws IllegalStateException
     */
    private void processOrder(FridgeOrder order) throws IllegalStateException {
        System.out.println("Received order: " + order.toString());
        if (currentOrders.size() < maxCapacity) {
            PerformanceTracker.getInstance().receivedOrder();

            if (currentOrders.size() == maxCapacity - 1) {
                orderSubscriber.pause();
            }
            DatabaseUtility.merge(emf, order);

            // Order all needed parts, then:
            // produce the product, then:
            // report that the order has been produced
            production.orderParts(order)
                    .thenCompose(o -> CompletableFuture.supplyAsync(() -> {
                        DatabaseUtility.merge(emf, o);
                        return o;
                    }))
                    .thenCompose(o -> production.produce(o, this.productionTimeFactor))
                    .thenAccept(this::reportFinishedOrder);
        } else {
            // This should never happen
            // If it does happen, current implementation of max capacity is faulty
            throw new IllegalStateException("Max capacity reached, didn't accept order: " + order.toString());
        }
    }

    /**
     * Handle a JMS-Message that indicates that the costs for a part needs to be updated
     */
    private final MessageListener updatePartCosts = m -> {
        var objectMessage = (ObjectMessage) m;
        try {
            var part = (Part) objectMessage.getObject();

            System.out.println("Received update costs for part: " + part);
            // Update the database with the received object
            DatabaseUtility.merge(emf, part);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    };

    /**
     * Report that a order has been finished (or: produced) to the HQ
     * @param order the finished order
     */
    private void reportFinishedOrder(FridgeOrder order) {
        if (order.getStatus() != OrderStatus.COMPLETED) return;
        System.out.println("Finished order " + order.toString());
        try {
            // Update the local database
            DatabaseUtility.merge(emf, order);
            // Publish the object
            finishedOrderPublisher.publish(order);
            // Remove the order from the currentOrder list
            currentOrders.remove(order);
            orderSubscriber.restart();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close all opened resources such as publisher, subscriber and database connections
     * @return thread that executes the closing
     */
    private Thread closeResources() {
        return new Thread(() -> {
            System.out.println("Shutdown headquarter");
            System.out.println("Closing ActiveMQ connections");
            if (finishedOrderPublisher != null) finishedOrderPublisher.close();
            if (reportPublisher != null) reportPublisher.close();
            if (orderSubscriber != null) orderSubscriber.close();
            if (updatePartCostSubscriber != null) updatePartCostSubscriber.close();
            if (emf != null) emf.close();
        });
    }
}
