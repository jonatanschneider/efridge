package de.thm.mni.vs.gruppe5.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Representation of orders used to send orders from CLI to HQ
 */
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

    /**
     * Validate the order
     * @return is order valid
     */
    @Override
    public boolean isValid() {
        // Check that for each item the selected product is valid (we have products with ids 0-4)
        // and check that no item has a negative quantity
        return getOrderProductIdsWithQuantity().entrySet().stream()
                .anyMatch(entrySet ->
                        entrySet.getKey() >= 0 &&
                        entrySet.getKey() < 5 &&
                        entrySet.getValue() > 0);
    }

    /**
     * Allows interactive creation of a new order via console
     *
     * @return order with user-defined products and quantity
     */
    @Override
    public FrontendItem interactiveCreation() {
        var scanner = new Scanner(System.in);
        var productIdsWithQuantity = new HashMap<Integer, Integer>();

        System.out.println("Enter customer id");
        setCustomerId(scanner.nextLine());

        var addProduct = true;
        do {
            System.out.println("Select product (1-5)");
            var productId = scanner.nextInt() - 1;
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
