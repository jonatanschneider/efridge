package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Location;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.SupportTicket;
import de.thm.mni.vs.gruppe5.common.model.TicketStatus;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import org.hibernate.service.spi.ServiceException;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManagerFactory;
import java.util.*;

/**
 * Represents a support center which is responsible for handling support tickets.
 */
public class SupportCenter {
    private final Location location;
    private int agents;
    private Set<SupportTicket> currentTickets;
    private Publisher finishedTicketPublisher;
    private IAgent agent;
    private Subscriber ticketSubscriber;
    private EntityManagerFactory emf;

    /**
     * Start a new support center, needs three parameters:
     * 0 - location (can be MEXICO or INDIA)
     * 1 - agent count - defines how many agents there are to process tickets at the same time
     * @param args 0 - location 1 - agent count
     */
    public static void main(String[] args) {
        Location location;
        int agents;

        try {
            location = Location.valueOf(args[0]);
            agents = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Invalid arguments");
            System.err.println("Usage: SupportCenter <LOCATION> <agents>");
            return;
        }

        try {
            var supportCenter = new SupportCenter(location, agents);
            Runtime.getRuntime().addShutdownHook(supportCenter.closeResources());

        } catch (ServiceException e) {
            System.err.println("Could not reach database. Exiting...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SupportCenter(Location location, int agents) throws JMSException {
        this.emf = DatabaseUtility.getEntityManager(location);
        this.location = location;
        this.agents = agents;
        this.currentTickets = Collections.synchronizedSet(new HashSet<>(agents));
        ticketSubscriber = new Subscriber(Config.TICKET_QUEUE, processTicketListener);
        finishedTicketPublisher = new Publisher(Config.FINISHED_TICKET_QUEUE);
        agent = new Agent();

        // Re-initialize previously interrupted tickets
        var em = emf.createEntityManager();
        var query = em.createQuery("SELECT t FROM SupportTicket t");
        ((List<SupportTicket>) query.getResultList())
                .stream()
                .filter(t -> t.getStatus() == TicketStatus.RECEIVED)
                .forEach(this::processTicket);
        em.close();
    }

    /**
     * Get incoming tickets
     */
    private final MessageListener processTicketListener = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var ticket = (SupportTicket) objectMessage.getObject();
            processTicket(ticket);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    };

    /**
     * Check available agents and process ticket if possible
     * @param ticket
     */
    private void processTicket(SupportTicket ticket) {
        if (!currentTickets.contains(ticket) && currentTickets.size() < agents) {
            System.out.println("Received ticket: " + ticket.toString());
            DatabaseUtility.merge(emf, ticket);
            if (currentTickets.size() == agents - 1) {
                ticketSubscriber.pause();
            }
            currentTickets.add(ticket);
            agent.handleTicket(ticket).thenAccept(this::reportProcessedTicket);
        }
    }

    /**
     * Persist a processed ticket and send it back
     * @param ticket
     */
    private void reportProcessedTicket(SupportTicket ticket) {
        System.out.println("Finished ticket " + ticket.toString());
        try {
            DatabaseUtility.merge(emf, ticket);
            finishedTicketPublisher.publish(ticket);
            currentTickets.remove(ticket);
            ticketSubscriber.restart();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private Thread closeResources() {
        return new Thread(() -> {
            System.out.println("Shutdown headquarter");
            System.out.println("Closing database connection");
            emf.close();
            System.out.println("Closing ActiveMQ connections");
            if (ticketSubscriber != null) ticketSubscriber.close();
            if (finishedTicketPublisher != null) finishedTicketPublisher.close();
        });
    }
}
