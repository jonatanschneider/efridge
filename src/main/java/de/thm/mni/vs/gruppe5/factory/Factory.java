package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.*;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class Factory {
    private IProduction production;
    private Publisher finishedOrderPublisher;

    public static void main(String[] args) {
        var factory = new Factory();

        try {
            factory.setup();
        } catch (JMSException e) {
            e.printStackTrace();
        }
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
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private final MessageListener processOrder = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var order = (FridgeOrder) objectMessage.getObject();

            System.out.println("Received order: " + order.toString());
            production.orderParts(order).thenCompose(o -> production.produce(o)).thenAccept(this::reportFinishedOrder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
