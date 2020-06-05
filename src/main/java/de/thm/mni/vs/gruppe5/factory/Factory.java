package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
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

    public static void main(String[] args) {
        var factory = new Factory(0.5f, 1);

        try {
            factory.setup();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Factory(float productionTimeFactor, int maxCapacity) {
        this.productionTimeFactor = productionTimeFactor;
        this.maxCapacity = maxCapacity;
        this.currentOrders = Collections.synchronizedList(new ArrayList<>(maxCapacity));
    }

    private void setup() throws JMSException {
        Config.initializeProducts();
        var orders = new Subscriber(Config.ORDER_QUEUE, processOrder);
        finishedOrderPublisher = new Publisher(Config.FINISHED_ORDER_QUEUE);
        production = new Production();
    }

    private void reportFinishedOrder(FridgeOrder order) {
        System.out.println("Finished order " + order.toString());
        try {
            finishedOrderPublisher.publish(order);
            currentOrders.remove(order);
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
                currentOrders.add(order);
                production.orderParts(order).thenCompose(o -> production.produce(o, this.productionTimeFactor)).thenAccept(this::reportFinishedOrder);
            } else {
                System.out.println("Max capacity reached, didn't accept order: " + order.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
