package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class KevinBaconPath implements HttpHandler {

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
        String baconNumber = "";
        HashMap<String, String> baconPath = new HashMap<>();

        if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        }
        else
            errorId = 400;

        try {
            baconNumber = db.getBaconNumber(actorId);
            baconPath = db.getBaconPath(actorId);
        } catch(NoPathException e) {
            errorId = 404;
        } catch(Exception e) {
            errorId = 500;
        }

        StringBuilder response = new StringBuilder("{\n" +
                "\t\"baconNumber\": " + "\"" + baconNumber + "\",\n" +
                "\t\"baconPath\": " + "[\n");
        for (Map.Entry<String, String> entry : baconPath.entrySet()) {
            String pathActorId = entry.getKey();
            String pathMovieId = entry.getValue();
            response.append("\t\t{\n" + "\t\t\t\"actorId\": " + "\"").append(pathActorId).append("\"\n").append("\t\t\t\"movieId\": ").append("\"").append(pathMovieId).append("\"\n").append("\t\t},\n");
        }
        response.delete(response.length()-2, response.length());
        response.append("\n\t]\n}");

        r.sendResponseHeaders(errorId, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
