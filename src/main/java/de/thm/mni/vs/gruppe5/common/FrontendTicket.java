package de.thm.mni.vs.gruppe5.common;

import com.google.gson.Gson;

import javax.jms.JMSException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class FrontendTicket implements FrontendItem {
    private String customerId;
    private Date creationTime;
    private boolean isClosed;
    private Date closingTime;
    private String text;

    @Override
    public boolean isValid() {
        if (customerId == null || customerId.isBlank() || creationTime == null || text == null || text.isBlank()) return false;
        if (closingTime != null && closingTime.before(creationTime)) return false;
        return !(isClosed && closingTime == null);
    }

    @Override
    public FrontendItem interactiveCreation() {
        var scanner = new Scanner(System.in);

        System.out.println("Enter customer id");
        customerId = scanner.nextLine();

        // TODO: Add more stuff

        return this;
    }

    @Override
    public void send(Publisher p) throws JMSException {
        if (!this.isValid()) {
            System.out.println("Ticket is invalid, not publishing");
            return;
        }
        System.out.println("Publish " + toString());
        p.publish(new Gson().toJson(this));
    }
}
