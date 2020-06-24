package de.thm.mni.vs.gruppe5.hq;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.*;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Date;
import java.util.List;

public class Headquarter {
    private Publisher orderPublisher;
    private Subscriber incomingOrdersSubscriber;
    private Subscriber finishedOrdersSubscriber;
    private Publisher ticketPublisher;
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
        this.products = Config.initializeProducts(Location.HEADQUARTER);
        var incomingOrders = new Subscriber(Config.INCOMING_ORDER_QUEUE, incomingOrderListener);
        var finishedOrders = new Subscriber(Config.FINISHED_ORDER_QUEUE, finishedOrderListener);
        var incomingTickets = new Subscriber(Config.INCOMING_TICKET_QUEUE, incomingTicketListener);
        var finishedTickets = new Subscriber(Config.FINISHED_TICKET_QUEUE, finishedTicketListener);
        orderPublisher = new Publisher(Config.ORDER_QUEUE);
        ticketPublisher = new Publisher(Config.TICKET_QUEUE);
    }

    private void processIncomingOrder(FridgeOrder order) throws JMSException {
        System.out.println("Send order to factories: " + order);
        DatabaseUtility.persist(em, order);
        orderPublisher.publish(order);
    }

    private void processIncomingTicket(SupportTicket ticket) throws JMSException {
        System.out.println("Send ticket to support centers: " + ticket);
        DatabaseUtility.persist(em, ticket);
        ticketPublisher.publish(ticket);
    }

    private final MessageListener incomingOrderListener = m -> {
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

    private final MessageListener incomingTicketListener = m -> {
        try {

            var objectMessage = (ObjectMessage) m;
            var frontendTicket = new Gson().fromJson((String) objectMessage.getObject(), FrontendTicket.class);

            if (!frontendTicket.isValid()){
                System.out.println("Discarding invalid ticket " + frontendTicket);
                return;
            }

            var supportTicket = buildSupportTicket(frontendTicket);

            System.out.println("Received support ticket: " + frontendTicket.toString());

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

    private SupportTicket buildSupportTicket(FrontendTicket frontendTicket) {
        var ticket = new SupportTicket();
        ticket.setCustomerId(frontendTicket.customerId);
        ticket.setCreationTime(frontendTicket.creationTime);
        ticket.setClosingTime(frontendTicket.closingTime);
        ticket.setClosed(frontendTicket.isClosed);
        ticket.setText(frontendTicket.text);
        return ticket;
    }

    private final MessageListener finishedOrderListener = m -> {
        if (m instanceof ObjectMessage) {
            try {
                var object = ((ObjectMessage) m).getObject();
                if (object instanceof FridgeOrder) {
                    System.out.println("Received finished order" + object);
                    DatabaseUtility.merge(em, object);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    };

    private final MessageListener finishedTicketListener = m -> {
        if (m instanceof ObjectMessage) {
            try {
                var object = ((ObjectMessage) m).getObject();
                if (object instanceof SupportTicket) {
                    var ticket = (SupportTicket) object;
                    if (!ticket.isClosed()) {
                        System.out.println("Received unfinished ticket" + object);
                        System.out.println("Sending back to SupportCenter");
                        ticketPublisher.publish(ticket);
                    } else {
                        System.out.println("Received finished ticket" + object);
                        ((SupportTicket) object).setClosingTime(new Date(System.currentTimeMillis()));
                        DatabaseUtility.merge(em, object);
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
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
