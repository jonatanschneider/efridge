package de.thm.mni.vs.gruppe5.hq;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendOrder;
import de.thm.mni.vs.gruppe5.common.Location;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.*;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class Headquarter {
    private Publisher orderPublisher;
    private Subscriber incomingOrdersSubscriber;
    private Subscriber finishedOrdersSubscriber;
    private List<Product> products;
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("eFridge-hq");
    private final EntityManager em = emf.createEntityManager();

    public static void main(String[] args) {
       try {
           var hq = new Headquarter();
           hq.setup();
           Runtime.getRuntime().addShutdownHook(hq.closeResources());
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private void setup() throws JMSException {
        products = Config.initializeProducts(Location.HEADQUARTER);
        incomingOrdersSubscriber = new Subscriber(Config.INCOMING_ORDER_QUEUE, incomingOrderListener);
        finishedOrdersSubscriber = new Subscriber(Config.FINISHED_ORDER_QUEUE, messageListener);
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

    private MessageListener incomingOrderListener = m -> {
        try {

            var objectMessage = (ObjectMessage) m;
            var frontendOrder = new Gson().fromJson((String) objectMessage.getObject(), FrontendOrder.class);

            if (!frontendOrder.isValid()){
                System.out.println("Discarding invalid order " + frontendOrder);
                return;
            }

            var fridgeOrder = buildFridgeOrder(frontendOrder);

            System.out.println("Received order: " + fridgeOrder.toString());

            processIncomingOrder(fridgeOrder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private FridgeOrder buildFridgeOrder(FrontendOrder frontendOrder) {
        var order = new FridgeOrder();
        order.setCustomerId(frontendOrder.customerId);
        frontendOrder.getOrderProductIdsWithQuantity().entrySet().stream()
                .map(entry -> new OrderItem(products.get(entry.getKey() - 1), entry.getValue()))
                .forEach(order.getOrderItems()::add);
        order.setStatus(OrderStatus.RECEIVED);
        return order;
    }

    private MessageListener messageListener = m -> {
        if (m instanceof ObjectMessage) {
            var objectMessage = (ObjectMessage) m;
            System.out.println(objectMessage.toString());
        }
    };

    private Thread closeResources() {
        return new Thread(() -> {
            System.out.println("Shutdown headquarter");
            System.out.println("Closing database connections");
            em.close();
            emf.close();
            System.out.println("Closing ActiveMQ connections");
            orderPublisher.close();
            finishedOrdersSubscriber.close();
            incomingOrdersSubscriber.close();
        });
    }
}
