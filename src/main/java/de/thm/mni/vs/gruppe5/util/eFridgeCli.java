package de.thm.mni.vs.gruppe5.util;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendItem;
import de.thm.mni.vs.gruppe5.common.FrontendOrder;
import de.thm.mni.vs.gruppe5.common.Publisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class eFridgeCli {
    private static Publisher incomingOrderPublisher;
    private static Publisher incomingTicketPublisher;

    public static void main(String[] args) {
        FrontendItem item;

        try {
            incomingOrderPublisher = new Publisher(Config.INCOMING_ORDER_QUEUE);
            incomingTicketPublisher = new Publisher(Config.INCOMING_TICKET_QUEUE);

            if (args.length > 0 && !args[0].isBlank()) item = FrontendItem.parseJsonFile(args[0], FrontendOrder.class);
            else item = new FrontendOrder().interactiveCreation();

            item.send(incomingOrderPublisher);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
