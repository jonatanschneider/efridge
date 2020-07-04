package de.thm.mni.vs.gruppe5.hq.controller;

import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.*;
import de.thm.mni.vs.gruppe5.util.DatabaseUtility;
import io.javalin.http.Context;

import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

public class TicketController {
    private final EntityManagerFactory emf;
    private final Publisher publisher;

    public TicketController(EntityManagerFactory emf, Publisher publisher) {
        this.emf = emf;
        this.publisher = publisher;
    }

    public void createTicket(Context ctx) throws JMSException {
        var frontendTicket = ctx.bodyAsClass(FrontendTicket.class);

        if (!frontendTicket.isValid()) {
            System.out.println("Discarding invalid ticket " + frontendTicket);
            ctx.status(400);
            return;
        }

        var ticket = buildSupportTicket(frontendTicket);
        ticket.setStatus(TicketStatus.RECEIVED);
        DatabaseUtility.persist(emf, ticket);
        System.out.println("Send ticket to support centers: " + ticket.toString());
        publisher.publish(ticket);
        ctx.status(201);
    }

    public void patchTicket(Context ctx) throws JMSException {
        var updatedTicket = ctx.bodyAsClass(TicketPatch.class);

        EntityManager em = emf.createEntityManager();
        var ticket = em.find(SupportTicket.class, ctx.pathParam("id"));

        if (ticket == null) {
            ctx.status(400);
            return;
        }

        if (ticket.getStatus() != TicketStatus.WAITING) {
            ctx.status(423);
            return;
        }

        em.getTransaction().begin();
        ticket.appendText(updatedTicket.getText());
        ticket.setStatus(TicketStatus.RECEIVED);
        em.getTransaction().commit();

        System.out.println("Send updated ticket to support centers: " + ticket.toString());
        publisher.publish(ticket);
        ctx.status(204);
        em.close();
    }

    public void getTicket(Context ctx) {
        EntityManager em = emf.createEntityManager();
        ctx.json(em.find(SupportTicket.class, ctx.pathParam("id")));
        em.close();
    }

    public void getTickets(Context ctx) {
        EntityManager em = emf.createEntityManager();
        String customerId = ctx.queryParam(Config.CUSTOMER_ID_PARAM);
        TypedQuery<SupportTicket> query;

        if (customerId != null) {
            query = em.createQuery("SELECT st FROM SupportTicket st WHERE st.customerId = :customerId", SupportTicket.class);
            query.setParameter("customerId", customerId);
        } else {
            query = em.createQuery("SELECT st FROM SupportTicket st", SupportTicket.class);
        }
        ctx.json(query.getResultList());
        em.close();
    }

    private SupportTicket buildSupportTicket(FrontendTicket frontendTicket) {
        var ticket = new SupportTicket();
        ticket.setCustomerId(frontendTicket.customerId);
        ticket.setCreationTime(frontendTicket.creationTime);
        ticket.setClosingTime(frontendTicket.closingTime);
        ticket.setStatus(frontendTicket.status);
        ticket.setText(frontendTicket.text);
        return ticket;
    }
}
