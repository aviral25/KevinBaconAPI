package ca.utoronto.utm.mcs;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.neo4j.driver.Values.parameters;

public class Neo4jBacon {

    private final Driver driver;
    private final Neo4jActor db = new Neo4jActor();

    public Neo4jBacon() {
        String uriDb = "bolt://localhost:7687";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","1234"));
    }

    public String getBaconNumber(String actorId) throws NoPathException {
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result n = tx.run("MATCH p=shortestPath((a:Actor {actorId: $x})-[:ACTED_IN*]-(b:Actor {actorId: \"nm0000102\"}))\n"
                        + "RETURN length(p)",
                parameters("x", actorId));
        if(n.hasNext()) {
            String r = n.next().toString();
            return r.substring(19, r.length()-2);
        }
        else
            throw new NoPathException();
    }

    public HashMap<String, String> getBaconPath(String actorId) throws NoPathException {
        HashMap<String, String> response = new HashMap<>();
        Session session = driver.session();
        Transaction tx = session.beginTransaction();

        if(actorId.equals("nm0000102")) {
            ArrayList<String> moviesList = db.getMovies("nm0000102");
            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(moviesList.size());
            response.put(actorId, moviesList.get(index));
            return response;
        }
        else {
            Result path = tx.run("MATCH p=shortestPath((a:Actor {actorId: $x})-[:ACTED_IN*]-(b:Actor {actorId: \"nm0000102\"}))\n"
                            + "RETURN nodes(p)",
                    parameters("x", actorId));
            if (path.hasNext()) {
                response = createMap(path.next().toString());
                return response;
            } else
                throw new NoPathException();
        }
    }

    public HashMap<String, String> createMap(String roughMap) {
        HashMap<String, String> response = new HashMap<>();
        String currentActor;
        String currentMovie;

        roughMap = roughMap.substring(0, roughMap.length()-2);

        currentActor = roughMap.substring(roughMap.lastIndexOf("node<") + 5, roughMap.lastIndexOf(">"));
        roughMap = roughMap.substring(0, roughMap.length()-9);

        while(roughMap.contains("node<")) {

            currentMovie = roughMap.substring(roughMap.lastIndexOf("node<") + 5, roughMap.lastIndexOf(">"));
            roughMap = roughMap.substring(0, roughMap.length()-9);

            response.put(currentActor, currentMovie);

            currentActor = roughMap.substring(roughMap.lastIndexOf("node<") + 5, roughMap.lastIndexOf(">"));
            roughMap = roughMap.substring(0, roughMap.length()-9);

            response.put(currentActor, currentMovie);
        }
        return response;
    }

    public void close() {
        driver.close();
    }
}
