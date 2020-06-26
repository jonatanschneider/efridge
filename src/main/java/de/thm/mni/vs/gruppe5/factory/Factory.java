package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import de.thm.mni.vs.gruppe5.factory.report.ReportTask;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import org.hibernate.criterion.Order;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

public class Factory {
    private final Location location;
    private final Publisher finishedOrderPublisher;
    private final Publisher reportPublisher;
    private Subscriber orderSubscriber;
    private IProduction production;
    private float productionTimeFactor;
    private int maxCapacity;
    private List<FridgeOrder> currentOrders;
    private EntityManagerFactory emf;


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
            Runtime.getRuntime().addShutdownHook(factory.closeResources());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Factory(Location location, float productionTimeFactor, int maxCapacity) throws JMSException {
        this.location = location;
        this.productionTimeFactor = productionTimeFactor;
        this.maxCapacity = maxCapacity;
        this.currentOrders = Collections.synchronizedList(new ArrayList<>(maxCapacity));
        this.emf = DatabaseUtility.getEntityManager(this.location);

        // Initialize publisher and subscriber
        this.orderSubscriber = new Subscriber(Config.ORDER_QUEUE, processOrder);
        this.finishedOrderPublisher = new Publisher(Config.FINISHED_ORDER_QUEUE);
        this.production = new Production();
        this.reportPublisher = new Publisher(Config.REPORT_QUEUE);

        // Initialize and start report task
        var reportTask = new ReportTask(reportPublisher);
        new Timer().scheduleAtFixedRate(reportTask, 0, reportTask.getPeriod());

        System.out.println("Factory - " + location.name()
                + " - productionTimeFactor: " + productionTimeFactor
                + " - maxCapacity: " + maxCapacity);

    }

    private final MessageListener processOrder = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var order = (FridgeOrder) objectMessage.getObject();

            System.out.println("Received order: " + order.toString());
            if (currentOrders.size() < maxCapacity) {
                PerformanceTracker.getInstance().receivedOrder(order);

                if (currentOrders.size() == maxCapacity - 1) {
                    orderSubscriber.pause();
                }
                DatabaseUtility.merge(emf, order);
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
        } catch (JMSException e) {
            e.printStackTrace();
        }
    };

    private void reportFinishedOrder(FridgeOrder order) {
        System.out.println("Finished order " + order.toString());
        try {
            DatabaseUtility.merge(emf, order);
            finishedOrderPublisher.publish(order);
            currentOrders.remove(order);
            orderSubscriber.restart();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private Thread closeResources() {
        return new Thread(() -> {
            System.out.println("Shutdown headquarter");
            System.out.println("Closing ActiveMQ connections");
            if (finishedOrderPublisher != null) {
                finishedOrderPublisher.close();
            }
            if (reportPublisher != null) {
                reportPublisher.close();
            }
            if (orderSubscriber != null) {
                orderSubscriber.close();
            }
            if (emf != null) {
                emf.close();
            }
        });
    }
}
