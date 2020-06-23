package de.thm.mni.vs.gruppe5.util;

import de.thm.mni.vs.gruppe5.common.*;

import java.util.Scanner;

public class eFridgeCli {

    public static void main(String[] args) {
        FrontendItem item = null;

        try {
            var incomingOrderPublisher = new Publisher(Config.INCOMING_ORDER_QUEUE);
            var incomingTicketPublisher = new Publisher(Config.INCOMING_TICKET_QUEUE);
            var scanner = new Scanner(System.in);

            if (args.length > 1 && !args[0].isBlank() && !args[1].isBlank()) {
                if (args[0].toLowerCase().trim().equals("order")) {
                    item = FrontendItem.parseJsonFile(args[1], FrontendOrder.class);
                    item.send(incomingOrderPublisher);
                }
                else if (args[0].toLowerCase().trim().equals("ticket")) {
                    item = FrontendItem.parseJsonFile(args[1], FrontendTicket.class);
                    item.send(incomingTicketPublisher);
                }
                else throw new IllegalArgumentException("Invalid publisher type: " + args[0]);

            } else {
                do {
                    System.out.println("Select type (order, ticket)");
                    var line = scanner.nextLine().toLowerCase().trim();
                    if (line.equals("order")) {
                        item = new FrontendOrder().interactiveCreation();
                        item.send(incomingOrderPublisher);
                    } else if (line.equals("ticket")) {
                        item = new FrontendTicket().interactiveCreation();
                        item.send(incomingTicketPublisher);
                    }
                } while (item == null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
