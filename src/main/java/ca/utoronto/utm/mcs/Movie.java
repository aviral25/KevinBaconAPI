package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Movie implements HttpHandler{

    private Neo4jMovie db = new Neo4jMovie();

    public Movie() {}

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

        String movieId = "";
        String name = null;
        ArrayList<String> actors = new ArrayList<>();

        if (deserialized.has("movieId")) {
            movieId = deserialized.getString("movieId");
        }
        else {
            errorId = 400;
        }

        try {
            name = db.getMovieName(movieId);
            actors = db.getActors(movieId);
        } catch(NoMovieException e) {
            errorId = 404;
        } catch(Exception e) {
            errorId = 500;
        }

        StringBuilder response = new StringBuilder("{\n" +
                "\t\"movieId\": " + "\"" + movieId + "\",\n" +
                "\t\"name\": " + "\"" + name + "\",\n" +
                "\t\"actors\": " + "[");
        for (String actor : actors) {
            response.append("\n\t\t\"").append(actor).append("\",");
        }
        if(actors.size() != 0) {
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
        String movieId = "";

        int errorId = 200;

        if(deserialized.has("name") && deserialized.has("movieId")) {
            name = deserialized.getString("name");
            movieId = deserialized.getString("movieId");
        }
        else
            errorId = 400;

        if(db.movieExists(movieId)) {
            errorId = 400;
        } else {
            try {
                db.addMovie(name, movieId);
            } catch (Exception e){
                errorId = 500;
            }
        }

        r.sendResponseHeaders(errorId, -1);
    }

}
