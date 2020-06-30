package de.thm.mni.vs.gruppe5.util;

import com.google.gson.Gson;
import de.thm.mni.vs.gruppe5.common.*;
import okhttp3.*;

import java.io.IOException;
import java.net.HttpURLConnection;
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
                do {
                    System.out.println("Select type (order, ticket, part)");
                    var line = scanner.nextLine().toLowerCase().trim();
                    if (line.equals("order")) {
                        item = new FrontendOrder().interactiveCreation();
                        post(Config.ORDER_URL, item);
                    } else if (line.equals("ticket")) {
                        item = new FrontendTicket().interactiveCreation();
                        post(Config.TICKET_URL, item);
                    } else if (line.equals("part")) {
                        System.out.println("Enter part id");
                        var partId = scanner.nextLine();
                        System.out.println("Enter new costs for part");
                        var costs = scanner.nextDouble();
                        scanner.nextLine();
                        post(Config.PARTS_URL + "/" + partId, costs);
                        return;
                    }
                } while (item == null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
