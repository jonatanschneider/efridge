package de.thm.mni.vs.gruppe5.util;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.*;
import de.thm.mni.vs.gruppe5.common.model.FridgeOrder;
import de.thm.mni.vs.gruppe5.common.model.Performance;
import okhttp3.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Scanner;

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
                    System.out.println("Enter action: (order, ticket, performance)");
                    var line = scanner.nextLine().toLowerCase().trim();
                    switch (line) {
                        case "order":
                            System.out.println("create or status");
                            var action = scanner.nextLine();

                            switch (action) {
                                case "create":
                                    item = new FrontendOrder().interactiveCreation();
                                    post(Config.ORDER_URL, item);
                                    break;
                                case "status":
                                    System.out.println("Enter customer id");
                                    var customerId = scanner.nextLine();
                                    FridgeOrder[] orders = getOrders(customerId);
                                    System.out.println(Arrays.toString(orders));
                                    break;
                            }

                            break;
                        case "ticket":
                            item = new FrontendTicket().interactiveCreation();
                            post(Config.TICKET_URL, item);
                            break;
                        case "performance":
                            System.out.println(Arrays.toString(getPerformance()));
                            break;
                        case "part":
                            System.out.println("Enter part id");
                            var partId = scanner.nextLine();
                            System.out.println("Enter new costs for part");
                            var costs = scanner.nextDouble();
                            scanner.nextLine();
                            post(Config.PARTS_URL + "/" + partId, costs);
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
