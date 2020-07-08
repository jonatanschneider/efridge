package de.thm.mni.vs.gruppe5.hq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.*;
import de.thm.mni.vs.gruppe5.hq.controller.OrderController;
import de.thm.mni.vs.gruppe5.hq.controller.TicketController;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import org.apache.activemq.command.ActiveMQObjectMessage;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.*;
import java.util.Date;

/**
 * Represents the eFridge Headquarter in London
 */
public class Headquarter {
    private final static Location location = Location.HEADQUARTER;
    private final Subscriber finishedOrdersSubscriber;
    private final Subscriber finishedTicketsSubscriber;
    private final Subscriber reportSubscriber;
    private final Subscriber dlqSubscriber;
    private Publisher orderPublisher;
    private Publisher updatePartCostPublisherUS;
    private Publisher updatePartCostPublisherCN;
    private Publisher ticketPublisher;
    private EntityManagerFactory emf;
    private Javalin server;

    public static void main(String[] args) {
        try {
            var hq = new Headquarter();
            // On exit close all opened resources
            Runtime.getRuntime().addShutdownHook(hq.closeResources());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Headquarter() throws JMSException {
        this.emf = DatabaseUtility.getEntityManager(location);

        // Initialize publisher and subscriber for all queues
        this.orderPublisher = new Publisher(Config.ORDER_QUEUE);
        this.ticketPublisher = new Publisher(Config.TICKET_QUEUE);
        this.updatePartCostPublisherUS = new Publisher(Config.UPDATE_PARTS_COST_QUEUE_US);
        this.updatePartCostPublisherCN = new Publisher(Config.UPDATE_PARTS_COST_QUEUE_CN);
        this.finishedOrdersSubscriber = new Subscriber(Config.FINISHED_ORDER_QUEUE, finishedOrderListener);
        this.finishedTicketsSubscriber = new Subscriber(Config.FINISHED_TICKET_QUEUE, finishedTicketListener);
        this.reportSubscriber = new Subscriber(Config.REPORT_QUEUE, incomingReportListener);
        this.dlqSubscriber = new Subscriber(Config.DEAD_LETTER_QUEUE, deadLetterQueueListener);

        // Initialize Javalin server settings
        Gson gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        // Create REST controller
        OrderController orderController = new OrderController(emf, orderPublisher);
        TicketController ticketController = new TicketController(emf, ticketPublisher);

        // Initialize Javalin server and routes
        server = Javalin.create().start(Config.SERVER_PORT);
        server.post(Config.ORDER_PATH, orderController::createOrder);
        server.get(Config.ORDER_PATH, orderController::getOrders);
        server.get(Config.ORDER_PATH  + "/:id", orderController::getOrder);
        server.get(Config.TICKET_PATH + "/:id", ticketController::getTicket);
        server.get(Config.TICKET_PATH, ticketController::getTickets);
        server.post(Config.TICKET_PATH, ticketController::createTicket);
        server.patch(Config.TICKET_PATH + "/:id", ticketController::patchTicket);
        server.patch(Config.PARTS_PATH + "/:id", this::updatePartCost);
        server.get(Config.PERFORMANCE_PATH, this::getPerformance);
    }

    /**
     * Handles POST request for changing the cost of a part
     * @param ctx javalin context
     * @throws JMSException
     */
    private void updatePartCost(Context ctx) throws JMSException {
        // Get the part which should be updated from the database
        var em = emf.createEntityManager();
        var part = em.find(Part.class, ctx.pathParam("id"));
        em.close();

        // Update the part and persist it
        var cost = ctx.bodyAsClass(double.class);
        part.setCost(cost);
        DatabaseUtility.merge(emf, part);

        // Publish the update to the US and CN factory
        System.out.println("Publish update part cost to USA " + part);
        this.updatePartCostPublisherUS.publish(part);
        System.out.println("Publish update part cost to China " + part);
        this.updatePartCostPublisherCN.publish(part);
    }

    /**
     * Handles get requests, returns all performance reports
     * @param ctx request's context
     */
    private void getPerformance(Context ctx) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Performance> query =
                em.createQuery("SELECT p FROM Performance p", Performance.class);
        ctx.json(query.getResultList());
        em.close();
    }

    /**
     * Handles JMS-Messages that indicate a order has finished
     */
    private final MessageListener finishedOrderListener = m -> {
        if (m instanceof ObjectMessage) {
            try {
                var object = ((ObjectMessage) m).getObject();
                if (object instanceof FridgeOrder) {
                    System.out.println("Received finished order: " + object);
                    // Persist the updated order into the hq database
                    DatabaseUtility.merge(emf, object);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * Handles JMS-Messages that indicate a ticket has been finished
     */
    private final MessageListener finishedTicketListener = m -> {
        if (m instanceof ObjectMessage) {
            try {
                var object = ((ObjectMessage) m).getObject();
                if (object instanceof SupportTicket) {
                    var ticket = (SupportTicket) object;

                    // Check if the ticket has been closed by a support agent
                    if (ticket.getStatus() == TicketStatus.CLOSED) {
                        System.out.println("Received finished ticket: " + object);
                        // We set the time in the HQ, so that we don't need to deal with local timezones from MX or IN
                        ticket.setClosingTime(new Date(System.currentTimeMillis()));
                    } else {
                        System.out.println("Received unfinished ticket: " + object);
                    }
                    // Update the corresponding dataset in our database
                    DatabaseUtility.merge(emf, ticket);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Handle a JMS-Message for an incoming KPI-report
     * Stores the report into the database
     */
    private final MessageListener incomingReportListener = m -> {
        if (m instanceof ObjectMessage) {
            try {
                var object = ((ObjectMessage) m).getObject();
                if (object instanceof Performance) {
                    var performance = (Performance) object;
                    System.out.println("Received performance: " + performance);
                    DatabaseUtility.persist(emf, performance);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Handles JMS-Messages from other queues which could not be delivered
     * Will attempt to re-send orders, tickets and part cost updates
     */
    private final MessageListener deadLetterQueueListener = m -> {
        if (m instanceof ObjectMessage) {
            try {
                var object = ((ObjectMessage) m).getObject();
                if (object instanceof FridgeOrder && ((FridgeOrder) object).getStatus() != OrderStatus.COMPLETED) {
                    System.out.println("Re-sending order to factories: " + object);
                    orderPublisher.publish(object);
                } else if (object instanceof SupportTicket && ((SupportTicket) object).getStatus() == TicketStatus.RECEIVED) {
                    System.out.println("Re-sending ticket to support centers: " + object);
                    ticketPublisher.publish(object);
                } else if (object instanceof Part) {
                    var destination = ((ActiveMQObjectMessage) m).getOriginalDestination().getPhysicalName();
                    switch (destination) {
                        case Config.UPDATE_PARTS_COST_QUEUE_CN -> {
                            System.out.println("Re-publish update part cost to China " + object);
                            updatePartCostPublisherCN.publish(object);
                        }
                        case Config.UPDATE_PARTS_COST_QUEUE_US -> {
                            System.out.println("Re-publish update part cost to USA " + object);
                            updatePartCostPublisherUS.publish(object);
                        }
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Close all opened resources such as publisher, subscriber and database connections
     * @return thread that executes the closing
     */
    private Thread closeResources() {
        return new Thread(() -> {
            System.out.println("Shutdown headquarter");
            System.out.println("Closing database connection");
            emf.close();
            System.out.println("Closing ActiveMQ connections");

            if (orderPublisher != null) orderPublisher.close();
            if (finishedOrdersSubscriber != null) finishedOrdersSubscriber.close();
            if (ticketPublisher != null) ticketPublisher.close();
            if (finishedTicketsSubscriber != null) finishedTicketsSubscriber.close();
            if (reportSubscriber != null) reportSubscriber.close();
            if (dlqSubscriber != null) dlqSubscriber.close();
            if (updatePartCostPublisherCN != null) updatePartCostPublisherCN.close();
            if (updatePartCostPublisherUS != null) updatePartCostPublisherUS.close();
        });
    }
}
