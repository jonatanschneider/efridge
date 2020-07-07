package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.model.SupportTicket;
import de.thm.mni.vs.gruppe5.common.model.TicketStatus;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an agent that is able to handle Support Tickets.
 */
public class Agent implements IAgent {
    /**
     * Handle / Process a support ticket
     * @param ticket ticket to handle / process
     * @return future resolving to an updated ticket after it has been processed
     */
    @Override
    public CompletableFuture<SupportTicket> handleTicket(SupportTicket ticket) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Handling ticket " + ticket.getId());

            if (!ticket.hasInit()) {
                ticket.initRandom(5);
            }
            try {
                ticket.complete();
            } catch (InterruptedException e) {
                System.out.println("Manually interrupting waiting time");
            }
            ticket.appendText("Test Message " + System.currentTimeMillis());

            if (Math.random() < Config.CHANCE_OF_CLOSING_TICKET) {
                ticket.setStatus(TicketStatus.CLOSED);
            } else {
                ticket.setStatus(TicketStatus.WAITING);
            }
            return ticket;
        });
    }
}
