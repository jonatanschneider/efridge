package de.thm.mni.vs.gruppe5.util;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.OrderStatus;
import de.thm.mni.vs.gruppe5.common.model.Performance;
import de.thm.mni.vs.gruppe5.common.model.SupportTicket;
import okhttp3.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Scanner;

/**
 * WARNING: This CLI tool is only for demonstration purposes and should not be used as a "real" frontend.
 * There is no real error handling in this tool, as is should only be used for creating and sending request
 * in the presentation or for testing purposes
 */
public class eFridgeCli {

    public static void main(String[] args) {
        FrontendItem item = null;

        try {
            var scanner = new Scanner(System.in);

            if (args.length > 1 && !args[0].isBlank() && !args[1].isBlank()) {
                if (args[0].toLowerCase().trim().equals("order")) {
                    item = FrontendItem.parseJsonFile(args[1], FrontendOrder.class);
                    post(Config.ORDER_URL, item);
                }
                else if (args[0].toLowerCase().trim().equals("ticket")) {
                    item = FrontendItem.parseJsonFile(args[1], FrontendTicket.class);
                    post(Config.TICKET_URL, item);
                }
                else throw new IllegalArgumentException("Invalid publisher type: " + args[0]);

            } else {
                while (true) {
                    System.out.println("Enter action: (order, ticket, performance, part)");
                    var line = scanner.nextLine().toLowerCase().trim();
                    String action;
                    switch (line) {
                        case "order":
                            System.out.println("create or status");
                            action = scanner.nextLine();

                            switch (action) {
                                case "create":
                                    item = new FrontendOrder().interactiveCreation();
                                    post(Config.ORDER_URL, item);
                                    break;
                                case "status":
                                    System.out.println("Enter customer id");
                                    var customerId = scanner.nextLine();
                                    FridgeOrder[] orders = getOrders(customerId);
                                    Arrays.stream(orders).map(FridgeOrder::toFormattedString).forEach(System.out::println);
                                    break;
                            }

                            break;
                        case "ticket":
                            System.out.println("create, status or update");
                            action = scanner.nextLine();

                            switch (action) {
                                case "create":
                                    item = new FrontendTicket().interactiveCreation();
                                    post(Config.TICKET_URL, item);
                                    break;
                                case "status":
                                    System.out.println("Enter customer id");
                                    var customerId = scanner.nextLine();
                                    SupportTicket[] tickets = getTickets(customerId);
                                    Arrays.stream(tickets).map(SupportTicket::toFormattedString).forEach(System.out::println);
                                    break;
                                case "update":
                                    System.out.println("Enter ticket id");
                                    var ticketId = scanner.nextLine();
                                    System.out.println("Enter text to attach");
                                    var text = scanner.nextLine();
                                    var ticketPatch = new TicketPatch(text);
                                    patch(Config.TICKET_URL + "/" + ticketId, ticketPatch);
                                    break;
                            }
                            break;
                        case "performance":
                            Arrays.stream(getPerformance()).map(Performance::toFormattedString).forEach(System.out::println);
                            break;
                        case "part":
                            System.out.println("Enter part id");
                            var partId = scanner.nextLine();
                            System.out.println("Enter new costs for part");
                            var costs = scanner.nextDouble();
                            scanner.nextLine();
                            patch(Config.PARTS_URL + "/" + partId, costs);
                            break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Performance[] getPerformance() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Config.PERFORMANCE_URL)
                .build();
        Response response = client.newCall(request).execute();
        return new Gson().fromJson(response.body().string(), Performance[].class);
    }

    private static FridgeOrder[] getOrders(String customerId) throws IOException {
        HttpUrl url = HttpUrl.parse(Config.ORDER_URL).newBuilder()
                .addQueryParameter(Config.CUSTOMER_ID_PARAM, customerId)
                .build();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return new Gson().fromJson(response.body().string(), FridgeOrder[].class);
    }

    private static SupportTicket[] getTickets(String customerId) throws IOException {
        HttpUrl url = HttpUrl.parse(Config.TICKET_URL).newBuilder()
                .addQueryParameter(Config.CUSTOMER_ID_PARAM, customerId)
                .build();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return new Gson().fromJson(response.body().string(), SupportTicket[].class);
    }

    private static boolean patch(String url, Object object) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String json = new Gson().toJson(object);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.code() == HttpURLConnection.HTTP_NO_CONTENT;
    }

    private static boolean post(String url, Object object) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String json = new Gson().toJson(object);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.code() == HttpURLConnection.HTTP_CREATED;
    }
}
