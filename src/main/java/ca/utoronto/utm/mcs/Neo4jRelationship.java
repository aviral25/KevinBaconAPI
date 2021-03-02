package ca.utoronto.utm.mcs;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class Neo4jRelationship {

    private final Neo4jActor actorDb = new Neo4jActor();
    private Neo4jMovie movieDb = new Neo4jMovie();

    private final Driver driver;

    public Neo4jRelationship() {
        String uriDb = "bolt://localhost:7687";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","1234"));
    }

    public boolean getRelationship(String actorId, String movieId) throws NoActorException, NoMovieException {

        String actorName = actorDb.getActorName(actorId);
        String movieName = movieDb.getMovieName(movieId);

        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        Result node_boolean = tx.run("MATCH (:Actor {actorId: $x})-[r:ACTED_IN]->(:Movie {movieId: $y})\n"
                + "RETURN r"
                ,parameters("x", actorId, "y", movieId) );
        return node_boolean.hasNext();
    }

    public void addRelationship(String actorId, String movieId) throws NoActorException, NoMovieException, RelationshipExistsException {

        String actorName = actorDb.getActorName(actorId);
        String movieName = movieDb.getMovieName(movieId);

        if(getRelationship(actorId, movieId))
            throw new RelationshipExistsException();

        Session session = driver.session();
        session.writeTransaction(tx -> tx.run("MATCH (a:Actor {actorId: $x}),"
                        + "(b:Movie {movieId: $y})\n"
                        + "MERGE (a)-[r:ACTED_IN]->(b)",
                parameters("x", actorId, "y", movieId)));
        session.close();
    }

    public void close() {
        driver.close();
    }
}
