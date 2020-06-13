package de.thm.mni.vs.gruppe5.util;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendOrder;
import de.thm.mni.vs.gruppe5.common.Publisher;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class eFridgeCli {
    private static final Scanner scanner = new Scanner(System.in);
    private static Publisher incomingOrderPublisher;

    public static void main(String[] args) {
        FrontendOrder order;

        try {
            incomingOrderPublisher = new Publisher(Config.INCOMING_ORDER_QUEUE);

            if (args.length > 0 && !args[0].equals("")) order = parseJsonFile(args[0]);
            else order = interactiveCreation();

            sendOrder(order);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static FrontendOrder interactiveCreation() {
        var order = new FrontendOrder();
        var productIdsWithQuantity = new HashMap<Integer, Integer>();

        System.out.println("Enter customer id");
        order.setCustomerId(scanner.nextLine());

        var addProduct = true;
        do {
            System.out.println("Select product (1-5)");
            var productId = scanner.nextInt();
            scanner.nextLine();

            System.out.println("Enter quantity");
            var quantity = scanner.nextInt();
            productIdsWithQuantity.put(productId, quantity);
            scanner.nextLine();

            System.out.println("Add another product? y/n");
            var answer = scanner.nextLine();
            addProduct = answer.trim().toLowerCase().equals("y");

        } while (addProduct);

        order.setOrderProductIdsWithQuantity(productIdsWithQuantity);
        return order;
    }

    private static FrontendOrder parseJsonFile(String path) throws IOException {
        var reader = Files.newBufferedReader(Paths.get(path));
        return new Gson().fromJson(reader, FrontendOrder.class);
    }

    private static void sendOrder(FrontendOrder order) throws JMSException {
        if (!order.isValid()) {
            System.out.println("Order is invalid, not publishing");
            return;
        }
        System.out.println("Publish " + order.toString());
        incomingOrderPublisher.publish(new Gson().toJson(order));
    }
}
