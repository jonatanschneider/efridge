package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class Factory {
    private IProduction production;
    private Publisher finishedOrderPublisher;
    private float productionTimeFactor;

    public static void main(String[] args) {
        var factory = new Factory(0.5f);

        try {
            factory.setup();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Factory(float productionTimeFactor) {
        this.productionTimeFactor = productionTimeFactor;
    }

    private void setup() throws JMSException {
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
            production.orderParts(order).thenCompose(o -> production.produce(o, this.productionTimeFactor)).thenAccept(this::reportFinishedOrder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
