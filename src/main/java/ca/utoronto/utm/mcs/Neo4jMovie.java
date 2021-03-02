package ca.utoronto.utm.mcs;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.util.ArrayList;

public class Neo4jMovie {

    private final Driver driver;

    public Neo4jMovie() {
        String uriDb = "bolt://localhost:7687";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","1234"));
    }

    public void addMovie(String name, String movieId) {
        Session session = driver.session();
        session.writeTransaction(tx -> tx.run("MERGE (m:Movie {name: $x, movieId: $y})",
                parameters("x", name, "y", movieId)));
        session.close();
    }

    public boolean movieExists(String movieId) {
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result name = tx.run("MATCH (m:Movie {movieId: $x})\n"
                        + "RETURN m.name",
                parameters("x", movieId));
        return name.hasNext();
    }

    public String getMovieName(String movieId) throws NoMovieException {
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result name = tx.run("MATCH (m:Movie {movieId: $x})\n"
                        + "RETURN m.name",
                parameters("x", movieId));
        if (name.hasNext()) {
            String response = name.next().toString();
            return response.substring(17, response.length()-3);
        } else
            throw new NoMovieException();
    }

    public ArrayList<String> getActors(String movieId) {
        ArrayList<String> actors = new ArrayList<>();
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        
        Result actor = tx.run("MATCH (a:Actor)-[r:ACTED_IN]->(m:Movie {movieId: $x})\n"
                        + "RETURN a.name",
                    parameters("x", movieId));

        while (actor.hasNext()) {
            String response = actor.next().toString();
            actors.add(response.substring(17, response.length()-3));
        }
        return actors;
    }

    public void close() {
        driver.close();
    }
}
