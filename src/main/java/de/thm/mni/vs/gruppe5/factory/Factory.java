package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.factory.report.ReportTask;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class Factory {
    private Location location;
    private IProduction production;
    private Publisher finishedOrderPublisher;
    private Publisher reportPublisher;
    private float productionTimeFactor;
    private int maxCapacity;
    private List<FridgeOrder> currentOrders;
    private Subscriber orderSubscriber;

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

        var factory = new Factory(location, productionTimeFactor, maxCapacity);

        try {
            factory.setup();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Factory(Location location, float productionTimeFactor, int maxCapacity) {
        this.location = location;
        this.productionTimeFactor = productionTimeFactor;
        this.maxCapacity = maxCapacity;
        this.currentOrders = Collections.synchronizedList(new ArrayList<>(maxCapacity));
        System.out.println("Factory - " + location.name()
                + " - productionTimeFactor: " + productionTimeFactor
                + " - maxCapacity: " + maxCapacity);
    }

    private void setup() throws JMSException {
        Config.initializeProducts(location);
        var orders = new Subscriber(Config.ORDER_QUEUE, processOrder);
        finishedOrderPublisher = new Publisher(Config.FINISHED_ORDER_QUEUE);
        production = new Production();
        reportPublisher = new Publisher(Config.REPORT_QUEUE);

        var reportTask = new ReportTask(reportPublisher);
        new Timer().scheduleAtFixedRate(reportTask, 0, Config.REPORT_INTERVAL * 1000);
    }

    private void reportFinishedOrder(FridgeOrder order) {
        System.out.println("Finished order " + order.toString());
        System.out.println("New Performance: " + PerformanceTracker.getInstance().toString());
        try {
            finishedOrderPublisher.publish(order);
            currentOrders.remove(order);
            orderSubscriber.restart();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private final MessageListener processOrder = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var order = (FridgeOrder) objectMessage.getObject();

            System.out.println("Received order: " + order.toString());
            if (currentOrders.size() < maxCapacity) {
                if (currentOrders.size() == maxCapacity - 1) {
                    orderSubscriber.pause();
                }
                currentOrders.add(order);
                production.orderParts(order).thenCompose(o -> production.produce(o, this.productionTimeFactor)).thenAccept(this::reportFinishedOrder);
            } else {
                // This should never happen
                // If it does happen, current implementation of max capacity is faulty
                throw new IllegalStateException("Max capacity reached, didn't accept order: " + order.toString());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    };
}
