package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Relationship implements HttpHandler{

    private Neo4jRelationship db = new Neo4jRelationship();

    public Relationship() {
    }

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
        String movieId = "";
        boolean hasRelationship = false;

        if (deserialized.has("actorId") && deserialized.has("movieId")) {
            actorId = deserialized.getString("actorId");
            movieId = deserialized.getString("movieId");
        } else
            errorId = 400;

        try {
            hasRelationship = db.getRelationship(actorId, movieId);
        } catch(NoActorException | NoMovieException e) {
            errorId = 404;
        } catch(Exception e) {
            errorId = 500;
        }

        String response = "{\n" +
                "\t\"actorId\": " + "\"" + actorId + "\",\n" +
                "\t\"movieId\": " + "\"" + movieId + "\",\n" +
                "\t\"hasRelationship\": " + hasRelationship + "\n" +
                "}";

        r.sendResponseHeaders(errorId, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void handlePut(HttpExchange r) throws JSONException, IOException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId = "";
        String movieId = "";

        int errorId = 200;

        if (deserialized.has("actorId") && deserialized.has("movieId")) {
            actorId = deserialized.getString("actorId");
            movieId = deserialized.getString("movieId");
        } else {
            errorId = 400;
        }

        try {
            db.addRelationship(actorId, movieId);
        } catch (NoActorException | NoMovieException e) {
            errorId = 404;
        } catch (RelationshipExistsException e){
            errorId = 400;
        } catch (Exception e){
            errorId = 500;
        }

        r.sendResponseHeaders(errorId, -1);
    }

}
