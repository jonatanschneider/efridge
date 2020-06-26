package de.thm.mni.vs.gruppe5.common;

import com.google.gson.Gson;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FrontendOrder implements FrontendItem {
    public String customerId;

    public Map<Integer, Integer> orderProductIdsWithQuantity;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Map<Integer, Integer> getOrderProductIdsWithQuantity() {
        return orderProductIdsWithQuantity;
    }

    public void setOrderProductIdsWithQuantity(Map<Integer, Integer> orderProductIdsWithQuantity) {
        this.orderProductIdsWithQuantity = orderProductIdsWithQuantity;
    }

    @Override
    public boolean isValid() {
        return getOrderProductIdsWithQuantity().entrySet().stream()
                .anyMatch(entrySet ->
                        entrySet.getKey() >= 0 &&
                        entrySet.getKey() < 5 &&
                        entrySet.getValue() > 0);
    }

    @Override
    public FrontendItem interactiveCreation() {
        var scanner = new Scanner(System.in);
        var productIdsWithQuantity = new HashMap<Integer, Integer>();

        System.out.println("Enter customer id");
        setCustomerId(scanner.nextLine());

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

        this.setOrderProductIdsWithQuantity(productIdsWithQuantity);
        return this;
    }

    @Override
    public String toString() {
        return "FrontendOrder{" +
                "customerId='" + customerId + '\'' +
                ", orderProductIdsWithQuantity=" + orderProductIdsWithQuantity +
                '}';
    }
}
