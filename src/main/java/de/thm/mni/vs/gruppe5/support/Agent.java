package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.util.TimeHelper;
import de.thm.mni.vs.gruppe5.common.model.SupportTicket;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class Agent implements IAgent {
    @Override
    public CompletableFuture<SupportTicket> handleTicket(SupportTicket ticket) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Handling ticket " + ticket.getId());

            TimeHelper.waitRandom(5);
            ticket.appendText("Test Message " + System.currentTimeMillis());

            if (Math.random() < Config.CHANCE_OF_CLOSING_TICKET) {
                ticket.setClosed(true);
                ticket.setClosingTime(new Date());
            }
            return ticket;
        });
    }
}
