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

public class Headquarter implements AutoCloseable {
    private Publisher orderPublisher;
    private Publisher ticketPublisher;
    private List<Product> products;
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("eFridge-hq");
    EntityManager em = emf.createEntityManager();

    public static void main(String[] args) {
       try {
           var hq = new Headquarter();
           hq.setup();
           // TODO closing of resources
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private void setup() throws JMSException {
        this.products = Config.initializeProducts(Location.HEADQUARTER);
        var incomingOrders = new Subscriber(Config.INCOMING_ORDER_QUEUE, incomingOrderListener);
        var finishedOrders = new Subscriber(Config.FINISHED_ORDER_QUEUE, messageListener);
        var incomingTickets = new Subscriber(Config.INCOMING_TICKET_QUEUE, incomingTicketListener);
        var finishedTickets = new Subscriber(Config.FINISHED_TICKET_QUEUE, messageListener);
        orderPublisher = new Publisher(Config.ORDER_QUEUE);
        ticketPublisher = new Publisher(Config.TICKET_QUEUE);
    }

    private void processIncomingOrder(FridgeOrder order) throws JMSException {
        System.out.println("Send order to factories: " + order.toString());
        persist(order);
        orderPublisher.publish(order);
    }

    private void processIncomingTicket(SupportTicket ticket) throws JMSException {
        System.out.println("Send ticket to factories: " + ticket.toString());
        persist(ticket);
        ticketPublisher.publish(ticket);
    }

    private void persist(FridgeOrder order) {
        em.getTransaction().begin();
        em.persist(order);
        em.getTransaction().commit();
    }

    private void persist(SupportTicket ticket) {
        em.getTransaction().begin();
        em.persist(ticket);
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

    private MessageListener incomingTicketListener= m -> {
        try {

            var objectMessage = (ObjectMessage) m;
            var supportTicket = new Gson().fromJson((String) objectMessage.getObject(), SupportTicket.class);

            // TODO: Validation?

            System.out.println("Received support ticket: " + supportTicket.toString());

            processIncomingTicket(supportTicket);
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

    @Override
    public void close() throws Exception {
        em.close();
        emf.close();
    }
}
