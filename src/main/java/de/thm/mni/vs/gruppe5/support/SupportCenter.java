package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Location;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.Subscriber;
import de.thm.mni.vs.gruppe5.common.model.SupportTicket;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportCenter {
    private int agents;
    private final Location location;
    private List<SupportTicket> currentTickets;
    private Publisher finishedTicketPublisher;
    private IAgent agent;
    private Subscriber ticketSubscriber;

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

        var supportCenter = new SupportCenter(location, agents);

        try {
            supportCenter.setup();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public SupportCenter(Location location, int agents) {
        this.location = location;
        this.agents = agents;
        this.currentTickets = Collections.synchronizedList(new ArrayList<>(agents));
    }

    private void setup() throws JMSException {
        ticketSubscriber = new Subscriber(Config.TICKET_QUEUE, processTicket);
        finishedTicketPublisher = new Publisher(Config.FINISHED_TICKET_QUEUE);
        agent = new Agent();
    }

    private void reportFinishedTicket(SupportTicket ticket) {
        System.out.println("Finished ticket " + ticket.toString());
        try {
            finishedTicketPublisher.publish(ticket);
            currentTickets.remove(ticket);
            ticketSubscriber.restart();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private final MessageListener processTicket = m -> {
        try {
            var objectMessage = (ObjectMessage) m;
            var ticket = (SupportTicket) objectMessage.getObject();

            System.out.println("Received ticket: " + ticket.toString());
            if (currentTickets.size() < agents) {
                if (currentTickets.size() == agents - 1) {
                    ticketSubscriber.pause();
                }
                currentTickets.add(ticket);
                agent.handleTicket(ticket).thenAccept(this::reportFinishedTicket);
            } else {
                // This should never happen
                // If it does happen, current implementation of max capacity is faulty
                throw new IllegalStateException("Max capacity reached, didn't accept order: " + ticket.toString());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    };
}
