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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportCenter {
    private final Location location;
    private int agents;
    private List<SupportTicket> currentTickets;
    private Publisher finishedTicketPublisher;
    private IAgent agent;
    private Subscriber ticketSubscriber;
    private EntityManagerFactory emf;


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
        this.currentTickets = Collections.synchronizedList(new ArrayList<>(agents));
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

    private final MessageListener processTicketListener = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var ticket = (SupportTicket) objectMessage.getObject();
            processTicket(ticket);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    };

    private void processTicket(SupportTicket ticket) {
        System.out.println("Received ticket: " + ticket.toString());
        DatabaseUtility.merge(emf, ticket);
        if (currentTickets.size() < agents) {
            if (currentTickets.size() == agents - 1) {
                ticketSubscriber.pause();
            }
            currentTickets.add(ticket);
            agent.handleTicket(ticket).thenAccept(this::reportFinishedTicket);
        } else {
            // This should never happen
            // If it does happen, current implementation of max capacity is faulty
            throw new IllegalStateException("Max capacity reached, didn't accept ticket: " + ticket.toString());
        }
    }

    private void reportFinishedTicket(SupportTicket ticket) {
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
            if (ticketSubscriber != null) {
                ticketSubscriber.close();
            }
            if (finishedTicketPublisher != null) {
                finishedTicketPublisher.close();
            }

        });
    }
}
