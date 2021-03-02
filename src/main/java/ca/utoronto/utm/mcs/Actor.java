package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Actor implements HttpHandler{

    private final Neo4jActor db = new Neo4jActor();

    public Actor() {}

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            }

            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange r) throws IOException, JSONException{
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        int errorId = 200;

        String actorId = "";
        String name = null;
        ArrayList<String> movies = new ArrayList<>();

        if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        }
        else
            errorId = 400;

        try {
            name = db.getActorName(actorId);
            movies = db.getMovies(actorId);
        } catch(NoActorException e) {
            errorId = 404;
        } catch(Exception e) {
            errorId = 500;
        }

        StringBuilder response = new StringBuilder("{\n" +
                "\t\"actorId\": " + "\"" + actorId + "\",\n" +
                "\t\"name\": " + "\"" + name + "\",\n" +
                "\t\"movies\": " + "[");
        for (String movie : movies) {
            response.append("\n\t\t\"").append(movie).append("\",");
        }
        if(movies.size() != 0) {
            response.delete(response.length()-2, response.length());
            response.append("\n\t]\n}");
        }
        else {
            response.append("]\n}");
        }


        r.sendResponseHeaders(errorId, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

    private void handlePut(HttpExchange r) throws JSONException, IOException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String name = "";
        String actorId = "";

        int errorId = 200;

        if(deserialized.has("name") && deserialized.has("actorId")) {
            name = deserialized.getString("name");
            actorId = deserialized.getString("actorId");
        }
        else {
            errorId = 400;
        }

        if(db.actorExists(actorId)) {
            errorId = 400;
        } else {
            try {
                db.addActor(name, actorId);
            } catch (Exception e){
                errorId = 500;
            }
        }

        r.sendResponseHeaders(errorId, -1);
    }

}
