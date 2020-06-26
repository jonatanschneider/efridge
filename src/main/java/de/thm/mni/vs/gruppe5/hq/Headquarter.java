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
import javax.persistence.Query;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Headquarter {
    private final static Location location = Location.HEADQUARTER;
    private final List<Product> products;
    private final Subscriber incomingOrdersSubscriber;
    private final Subscriber finishedOrdersSubscriber;
    private final Subscriber incomingTicketsSubscriber;
    private final Subscriber finishedTicketsSubscriber;
    private Publisher orderPublisher;
    private Publisher ticketPublisher;
    private EntityManagerFactory emf;


    public static void main(String[] args) {
       try {
           var hq = new Headquarter();
           Runtime.getRuntime().addShutdownHook(hq.closeResources());
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private Headquarter() throws JMSException {
        this.emf = DatabaseUtility.getEntityManager(location);

        var em = emf.createEntityManager();
        Query query = em.createQuery("SELECT p FROM Product p");
        this.products = query.getResultList();
        this.products.sort(Comparator.comparing(Product::getId));
        em.close();

        this.incomingOrdersSubscriber = new Subscriber(Config.INCOMING_ORDER_QUEUE, incomingOrderListener);
        this.finishedOrdersSubscriber = new Subscriber(Config.FINISHED_ORDER_QUEUE, finishedOrderListener);
        this.incomingTicketsSubscriber = new Subscriber(Config.INCOMING_TICKET_QUEUE, incomingTicketListener);
        this.finishedTicketsSubscriber = new Subscriber(Config.FINISHED_TICKET_QUEUE, finishedTicketListener);
        this.orderPublisher = new Publisher(Config.ORDER_QUEUE);
        this.ticketPublisher = new Publisher(Config.TICKET_QUEUE);
    }

    private void processIncomingOrder(FridgeOrder order) throws JMSException {
        System.out.println("Send order to factories: " + order);
        DatabaseUtility.persist(emf, order);
        this.orderPublisher.publish(order);
    }

    private void processIncomingTicket(SupportTicket ticket) throws JMSException {
        System.out.println("Send ticket to support centers: " + ticket);
        DatabaseUtility.persist(emf, ticket);
        this.ticketPublisher.publish(ticket);
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
                    DatabaseUtility.merge(emf, object);
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
                        DatabaseUtility.merge(emf, object);
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
            System.out.println("Closing database connection");
            emf.close();
            System.out.println("Closing ActiveMQ connections");
            if (orderPublisher != null) {
                orderPublisher.close();
            }
            if (incomingOrdersSubscriber != null) {
                incomingOrdersSubscriber.close();
            }
            if (finishedOrdersSubscriber != null) {
                finishedOrdersSubscriber.close();
            }

            if (ticketPublisher != null) {
                ticketPublisher.close();
            }
            if (incomingTicketsSubscriber != null) {
                incomingTicketsSubscriber.close();
            }
            if (finishedTicketsSubscriber != null) {
                finishedTicketsSubscriber.close();
            }
        });
    }
}
