package de.thm.mni.vs.gruppe5.common;

import com.google.gson.Gson;

import javax.jms.JMSException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class FrontendTicket implements FrontendItem {
    public String customerId;
    public Date creationTime;
    public boolean isClosed;
    public Date closingTime;
    public String text;

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

        System.out.println("Enter text");
        text = "";
        String line;
        do {
            line = scanner.nextLine().trim() + "\n";
            text += line;
        } while (!line.isBlank());
        text = text.trim();
        creationTime = new Date(System.currentTimeMillis());
        isClosed = false;

        return this;
    }
}
