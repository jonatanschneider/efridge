package de.thm.mni.vs.gruppe5.hq;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.*;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.Persistence;
import java.util.HashSet;

public class Headquarter {
    private Publisher orderPublisher;

    public static void main(String[] args) {
       var hq = new Headquarter();
       try {
           hq.setup();

           // TODO TMP: demo order
           hq.processIncomingOrder(hq.getDemoOrder());
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
        persist(order);
        orderPublisher.publish(order);
    }

    private void persist(FridgeOrder order) {
        var emf = Persistence.createEntityManagerFactory("eFridge");
        var em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(order);
        em.getTransaction().commit();
        em.close();
        emf.close();
    }

    private FridgeOrder getDemoOrder() {
        var part = new Part(2.4, Supplier.CoolMechanics);

        var set = new HashSet<ProductPart>();
        var productPart = new ProductPart(part, 2);
        set.add(productPart);
        var product = new Product("Tolles Produkt", 4, set);

        var set2 = new HashSet<OrderItem>();
        var item = new OrderItem(product, 2);
        set2.add(item);

        return new FridgeOrder("customerId", set2, OrderStatus.RECEIVED, false);
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
