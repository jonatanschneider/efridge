package de.thm.mni.vs.gruppe5.hq;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.*;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashSet;
import java.util.List;

public class Headquarter implements AutoCloseable {
    private Publisher orderPublisher;
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("eFridge");
    EntityManager em = emf.createEntityManager();

    public static void main(String[] args) {
       try (var hq = new Headquarter()) {
           hq.setup();

           // TODO TMP: demo order
           hq.processIncomingOrder(hq.getDemoOrder());

       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private void setup() throws JMSException {
        Config.initializeProducts();
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
        em.getTransaction().begin();
        em.persist(order);
        em.getTransaction().commit();
    }

    private FridgeOrder getDemoOrder() {
        var set = new HashSet<OrderItem>();
        var item = new OrderItem(em.find(Product.class, "1"), 2);
        set.add(item);

        return new FridgeOrder("customerId", set, OrderStatus.RECEIVED, false);
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

    @Override
    public void close() throws Exception {
        em.close();
        emf.close();
    }
}
