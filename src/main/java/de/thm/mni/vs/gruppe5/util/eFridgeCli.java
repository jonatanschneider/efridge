package de.thm.mni.vs.gruppe5.util;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.FrontendOrder;
import de.thm.mni.vs.gruppe5.common.Publisher;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class eFridgeCli {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        var productIdsWithQuantity = new HashMap<Integer, Integer>();
        System.out.println("Enter customer id");
        var customerId = scanner.nextLine();

        var addProduct = true;
        do {
            System.out.println("Select product (1-5)");
            var productId = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter quantity");
            var quantity = scanner.nextInt();
            scanner.nextLine();
            productIdsWithQuantity.put(productId, quantity);
            System.out.println("Add another product? y/n");
            var answer = scanner.nextLine();
            addProduct = answer.trim().toLowerCase().equals("y");
        } while(addProduct);


        //TODO validation

        try {
            sendOrder(customerId, productIdsWithQuantity);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

    }

    private static void sendOrder(String customerId, Map<Integer, Integer> productIdsWithQuantity) throws JMSException {
        var incomingOrder = new Publisher(Config.INCOMING_ORDER_QUEUE);
        var order = new FrontendOrder();
        Gson gson = new Gson();

        order.setCustomerId(customerId);
        order.setOrderProductIdsWithQuantity(productIdsWithQuantity);
        System.out.println("Publish");
        incomingOrder.publish(gson.toJson(order));
    }
}
