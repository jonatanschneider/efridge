package de.thm.mni.vs.gruppe5.common;

import de.thm.mni.vs.gruppe5.common.model.TicketStatus;

import java.util.Date;
import java.util.Scanner;

public class FrontendTicket implements FrontendItem {
    public String customerId;
    public Date creationTime;
    public TicketStatus status;
    public Date closingTime;
    public String text;

    @Override
    public boolean isValid() {
        if (customerId == null || customerId.isBlank() || creationTime == null || text == null || text.isBlank()) return false;
        return closingTime == null || !closingTime.before(creationTime);
    }

    @Override
    public FrontendItem interactiveCreation() {
        var scanner = new Scanner(System.in);

        System.out.println("Enter customer id");
        customerId = scanner.nextLine();

        System.out.println("Enter text");
        text = "";
        String line;
        do {
            line = scanner.nextLine().trim() + "\n";
            text += line;
        } while (!line.isBlank());
        text = text.trim();
        creationTime = new Date(System.currentTimeMillis());

        return this;
    }
}
