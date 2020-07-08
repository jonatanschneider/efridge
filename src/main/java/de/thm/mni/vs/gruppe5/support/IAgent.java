package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.common.model.SupportTicket;

import java.util.concurrent.CompletableFuture;

/**
 * Interface description of an agent that is able to handle Support Tickets.
 */
public interface IAgent {
    /**
     * Handle / Process a support ticket
     * @param ticket ticket to handle / process
     * @return future resolving to an updated ticket after it has been processed
     */
    CompletableFuture<SupportTicket> handleTicket(SupportTicket ticket);
}
