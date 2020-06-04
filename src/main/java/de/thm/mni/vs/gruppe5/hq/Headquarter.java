package de.thm.mni.vs.gruppe5.hq;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.*;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class Headquarter {
    private Publisher orderPublisher;

    public static void main(String[] args) {
       var hq = new Headquarter();
       try {
           hq.setup();

       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private void setup() throws JMSException {
        var incomingOrders = new Subscriber(Config.INCOMING_ORDER_QUEUE, incomingOrderListener);
        var finishedOrders = new Subscriber(Config.FINISHED_ORDER_QUEUE, messageListener);
        orderPublisher = new Publisher(Config.ORDER_QUEUE);
    }

    private void processIncomingOrder(FridgeOrder order) throws JMSException {
        System.out.println("Send order to factories: " + order.toString());
        orderPublisher.publish(order);
    }

    private MessageListener incomingOrderListener = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var order = (FridgeOrder) objectMessage.getObject();

            System.out.println("Received order: " + order.toString());

            processIncomingOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private MessageListener messageListener = m -> {
        if (m instanceof ObjectMessage) {
            var objectMessage = (ObjectMessage) m;
            System.out.println(objectMessage.toString());
        }
    };
}
