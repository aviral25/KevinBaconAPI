package ca.utoronto.utm.mcs;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.util.ArrayList;

public class Neo4jActor {

    private final Driver driver;

    public Neo4jActor() {
        String uriDb = "bolt://localhost:7687";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","1234"));
    }

    public void addActor(String name, String actorId) {
        Session session = driver.session();
        session.writeTransaction(tx -> tx.run("MERGE (a:Actor {name: $x, actorId: $y})",
                parameters("x", name, "y", actorId)));
        session.close();
    }

    public boolean actorExists(String actorId) {
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result name = tx.run("MATCH (a:Actor {actorId: $x})\n"
                        + "RETURN a.name",
                parameters("x", actorId));
        return name.hasNext();
    }

    public String getActorName(String actorId) throws NoActorException {
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result name = tx.run("MATCH (a:Actor {actorId: $x})\n"
                        + "RETURN a.name",
                parameters("x", actorId));
        if (name.hasNext()) {
            String response = name.next().toString();
            return response.substring(17, response.length()-3);
        } else
            throw new NoActorException();
    }

    public ArrayList<String> getMovies(String actorId) {
        ArrayList<String> movies = new ArrayList<>();
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result movie= tx.run("MATCH (a:Actor {actorId: $x})-[r:ACTED_IN]->(m:Movie)\n"
                        + "RETURN m.movieId",
                    parameters("x", actorId));
        while (movie.hasNext()) {
            String response = movie.next().toString();
            movies.add(response.substring(20, response.length()-3));
        }
        return movies;
    }

    public void close() {
        driver.close();
    }
}
