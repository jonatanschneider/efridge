package de.thm.mni.vs.gruppe5.hq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.*;
import de.thm.mni.vs.gruppe5.hq.controller.OrderController;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import org.apache.activemq.command.ActiveMQObjectMessage;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.*;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Headquarter {
    private final static Location location = Location.HEADQUARTER;
    private final Subscriber finishedOrdersSubscriber;
    private final Subscriber finishedTicketsSubscriber;
    private Subscriber reportSubscriber;
    private Subscriber dlqSubscriber;
    private Publisher orderPublisher;
    private Publisher updatePartCostPublisherUS;
    private Publisher updatePartCostPublisherCN;
    private Publisher ticketPublisher;
    private EntityManagerFactory emf;
    private Javalin server;

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

        this.finishedOrdersSubscriber = new Subscriber(Config.FINISHED_ORDER_QUEUE, finishedOrderListener);
        this.finishedTicketsSubscriber = new Subscriber(Config.FINISHED_TICKET_QUEUE, finishedTicketListener);
        this.reportSubscriber = new Subscriber(Config.REPORT_QUEUE, incomingReportListener);
        this.dlqSubscriber = new Subscriber(Config.DEAD_LETTER_QUEUE, deadLetterQueueListener);
        this.orderPublisher = new Publisher(Config.ORDER_QUEUE);
        this.ticketPublisher = new Publisher(Config.TICKET_QUEUE);
        this.updatePartCostPublisherUS = new Publisher(Config.UPDATE_PARTS_COST_TOPIC_US);
        this.updatePartCostPublisherCN = new Publisher(Config.UPDATE_PARTS_COST_TOPIC_CN);

        Gson gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        OrderController orderController = new OrderController(emf, orderPublisher);

        server = Javalin.create().start(Config.SERVER_PORT);
        server.post(Config.ORDER_PATH, orderController::createOrder);
        server.get(Config.ORDER_PATH, orderController::getOrders);
        server.get(Config.ORDER_PATH  + "/:id", orderController::getOrder);
        server.post(Config.TICKET_PATH, this::createTicket);
        server.post(Config.PARTS_PATH + "/:id", this::updatePart);
        server.get(Config.TICKET_PATH + "/:id", this::getTicket);
        server.get(Config.PERFORMANCE_PATH, this::getPerformance);
    }

    private void getTicket(Context ctx) {
        EntityManager em = emf.createEntityManager();
        ctx.json(em.find(SupportTicket.class, ctx.pathParam("id")));
        em.close();
    }

    private void createTicket(Context ctx) throws JMSException {
        var frontendTicket = ctx.bodyAsClass(FrontendTicket.class);

        if (!frontendTicket.isValid()) {
            System.out.println("Discarding invalid ticket " + frontendTicket);
            ctx.status(400);
            return;
        }

        var ticket = buildSupportTicket(frontendTicket);
        DatabaseUtility.persist(emf, ticket);
        System.out.println("Send ticket to support centers: " + ticket.toString());
        ticketPublisher.publish(ticket);
        ctx.status(201);
    }

    private void updatePart(Context ctx) throws JMSException {
        var em = emf.createEntityManager();
        var part = em.find(Part.class, ctx.pathParam("id"));
        var cost = ctx.bodyAsClass(double.class);
        em.close();
        part.setCost(cost);

        DatabaseUtility.merge(emf, part);

        System.out.println("Publish update part cost to USA " + part);
        this.updatePartCostPublisherUS.publish(part);
        System.out.println("Publish update part cost to China " + part);
        this.updatePartCostPublisherCN.publish(part);
    }

    private void getPerformance(Context ctx) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Performance> query =
                em.createQuery("SELECT p FROM Performance p", Performance.class);
        ctx.json(query.getResultList());
        em.close();
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

    private final MessageListener deadLetterQueueListener = m -> {
        if (m instanceof ObjectMessage) {
            try {
                var object = ((ObjectMessage) m).getObject();
                if (object instanceof FridgeOrder && ((FridgeOrder) object).getStatus() != OrderStatus.COMPLETED) {
                    System.out.println("Re-sending order to factories: " + object);
                    orderPublisher.publish(object);
                } else if (object instanceof SupportTicket && !((SupportTicket) object).isClosed()) {
                    System.out.println("Re-sending ticket to support centers: " + object);
                    ticketPublisher.publish(object);
                } else if (object instanceof Part) {
                    var destination = ((ActiveMQObjectMessage) m).getOriginalDestination().getPhysicalName();
                    switch (destination) {
                        case Config.UPDATE_PARTS_COST_TOPIC_CN -> {
                            System.out.println("Re-publish update part cost to China " + object);
                            updatePartCostPublisherCN.publish(object);
                        }
                        case Config.UPDATE_PARTS_COST_TOPIC_US -> {
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

    private Thread closeResources() {
        return new Thread(() -> {
            System.out.println("Shutdown headquarter");
            System.out.println("Closing database connection");
            emf.close();
            System.out.println("Closing ActiveMQ connections");

            if (orderPublisher != null) {
                orderPublisher.close();
            }
            if (finishedOrdersSubscriber != null) {
                finishedOrdersSubscriber.close();
            }

            if (ticketPublisher != null) {
                ticketPublisher.close();
            }
            if (finishedTicketsSubscriber != null) {
                finishedTicketsSubscriber.close();
            }

            if (reportSubscriber != null) {
                reportSubscriber.close();
            }

            if (dlqSubscriber != null) {
                dlqSubscriber.close();
            }

            if (updatePartCostPublisherCN != null) {
                updatePartCostPublisherCN.close();
            }

            if (updatePartCostPublisherUS != null) {
                updatePartCostPublisherUS.close();
            }
        });
    }
}
