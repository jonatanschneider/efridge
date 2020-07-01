package de.thm.mni.vs.gruppe5.common;

import de.thm.mni.vs.gruppe5.common.model.TicketStatus;
import java.util.Date;
import java.util.Scanner;

/**
 * Representation of support tickets used to send newly created tickets from CLI to HQ
 */
public class FrontendTicket implements FrontendItem {
    public String customerId;
    public Date creationTime;
    public TicketStatus status;
    public Date closingTime;
    public String text;

    /**
     * Validate tickets
     * @return whether or not a ticket is valid
     */
    @Override
    public boolean isValid() {
        if (customerId == null || customerId.isBlank() || creationTime == null || text == null || text.isBlank()) return false;
        return closingTime == null || !closingTime.before(creationTime);
    }

    /**
     * Allows interactive creation of a new ticket via console
     * @return open ticket with user-defined texts and current date as opening date
     */
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
