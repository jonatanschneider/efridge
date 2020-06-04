package de.thm.mni.vs.gruppe5.hq;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;

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
        var incomingOrders = new Subscriber(Config.INCOMING_ORDER_QUEUE, messageListener);
        var finishedOrders = new Subscriber(Config.FINISHED_ORDER_QUEUE, messageListener);
        orderPublisher = new Publisher(Config.ORDER_QUEUE);
    }

    private MessageListener messageListener = m -> {
        if (m instanceof ObjectMessage) {
            var objectMessage = (ObjectMessage) m;
            System.out.println(objectMessage.toString());
        }
    };
}
