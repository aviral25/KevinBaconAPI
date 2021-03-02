package ca.utoronto.utm.mcs;

public class RelationshipExistsException extends Exception {
    public String toString() {
        return "Relationship already exists.";
    }
}