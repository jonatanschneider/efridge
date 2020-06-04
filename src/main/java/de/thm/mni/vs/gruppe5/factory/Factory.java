package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.Config;
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

    public static void main(String[] args) {
        var factory = new Factory();

        try {
            factory.setup();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void setup() throws JMSException {
        var orders = new Subscriber(Config.ORDER_QUEUE, processOrder);
        production = new Production();
    }

    private final MessageListener processOrder = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var order = (FridgeOrder) objectMessage.getObject();

            System.out.println("Received order: " + order.toString());
            production.orderParts(order).thenCompose(o -> production.produce(o)).thenRun(() -> System.out.println("Fertig"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
