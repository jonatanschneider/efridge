package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.util.TimeHelper;
import de.thm.mni.vs.gruppe5.common.model.SupportTicket;

import java.util.concurrent.CompletableFuture;

public class Agent implements IAgent {
    @Override
    public CompletableFuture<SupportTicket> handleTicket(SupportTicket ticket) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Handling ticket " + ticket.getId());

            TimeHelper.waitRandom(5);
            ticket.appendText("Test Message " + System.currentTimeMillis());
            // TODO: close ticket in some cases and set timestamp accordingly
            return ticket;
        });
    }
}
