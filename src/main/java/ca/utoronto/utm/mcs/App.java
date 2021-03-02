package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        server.createContext("/api/v1/addActor", new Actor());
        server.createContext("/api/v1/getActor", new Actor());
        server.createContext("/api/v1/addMovie", new Movie());
        server.createContext("/api/v1/getMovie", new Movie());
        server.createContext("/api/v1/addRelationship", new Relationship());
        server.createContext("/api/v1/hasRelationship", new Relationship());
        server.createContext("/api/v1/computeBaconNumber", new KevinBaconNumber());
        server.createContext("/api/v1/computeBaconPath", new KevinBaconPath());

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
