package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class KevinBaconNumber implements HttpHandler {

    Neo4jBacon db = new Neo4jBacon();

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        int errorId = 200;

        String actorId = "";

        if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        }
        else
            errorId = 400;

        StringBuilder response;

        if(actorId.equals("nm0000102")) {
            response = new StringBuilder("{\n"
                    + "\tbaconNumber: " + "\"0\"\n"
                    + "}");
        } else {
            String n = "";
            try {
                n = db.getBaconNumber(actorId);
            } catch (NoPathException e) {
                errorId = 404;
            } catch (Exception e) {
                errorId = 500;
            }

            response = new StringBuilder("{\n"
                    + "\tbaconNumber: " + "\"" + n + "\"\n"
                    + "}");
        }

        r.sendResponseHeaders(errorId, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
