package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.model.SupportTicket;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class Agent implements IAgent {
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
                ticket.setClosed(true);
                ticket.setClosingTime(new Date());
            }
            return ticket;
        });
    }
}
