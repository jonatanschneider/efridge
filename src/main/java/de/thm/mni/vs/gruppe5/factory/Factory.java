package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Factory {
    private IProduction production;
    private Publisher finishedOrderPublisher;
    private float productionTimeFactor;
    private int maxCapacity;
    private List<FridgeOrder> currentOrders;
    private Subscriber orderSubscriber;

    public static void main(String[] args) {
        var factory = new Factory(0.5f, 2);

        try {
            factory.setup(Location.CHINA);
            Runtime.getRuntime().addShutdownHook(factory.closeResources());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Factory(float productionTimeFactor, int maxCapacity) {
        this.productionTimeFactor = productionTimeFactor;
        this.maxCapacity = maxCapacity;
        this.currentOrders = Collections.synchronizedList(new ArrayList<>(maxCapacity));
    }

    private void setup(Location location) throws JMSException {
        Config.initializeProducts(location);
        orderSubscriber = new Subscriber(Config.ORDER_QUEUE, processOrder);
        finishedOrderPublisher = new Publisher(Config.FINISHED_ORDER_QUEUE);
        production = new Production();
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

    private Thread closeResources() {
        return new Thread(() -> {
            System.out.println("Shutdown headquarter");
            System.out.println("Closing ActiveMQ connections");
            finishedOrderPublisher.close();
            orderSubscriber.close();
        });
    }
}
