package de.thm.mni.vs.gruppe5.support;

import de.thm.mni.vs.gruppe5.common.model.SupportTicket;

import java.util.concurrent.CompletableFuture;

public interface IAgent {
    CompletableFuture<SupportTicket> handleTicket(SupportTicket ticket);
}
