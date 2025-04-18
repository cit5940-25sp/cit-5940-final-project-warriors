import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Player {
    String username;
    Map<String, Integer> connections;
    Set<String> correctGuesses;
    Set<String> incorrectGuesses;

    /**
     * Initialize a new Player
     */
    public Player(String username) {
        this.username = username;
        this.connections = new HashMap<>();
        this.correctGuesses = new HashSet<>();
        this.incorrectGuesses = new HashSet<>();
    }

    /**
     *
     * @return the username of the player
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the player
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return the connections map of the player
     */
    public Map<String, Integer> getConnections() {
        return connections;
    }

    /**
     * Sets the connections map of the player
     * @param connections
     */
    public void setConnections(Map<String, Integer> connections) {
        this.connections = connections;
    }

    /**
     *
     * @return the set of correct guesses
     */
    public Set<String> getCorrectGuesses() {
        return correctGuesses;
    }

    /**
     * Sets the set of correct guesses
     * @param correctGuesses
     */
    public void setCorrectGuesses(Set<String> correctGuesses) {
        this.correctGuesses = correctGuesses;
    }

    /**
     *
     * @return the set of incorrectguesses
     */
    public Set<String> getIncorrectGuesses() {
        return incorrectGuesses;
    }

    /**
     * Sets the set of incorrect guesses
     * @param incorrectGuesses
     */
    public void setIncorrectGuesses(Set<String> incorrectGuesses) {
        this.incorrectGuesses = incorrectGuesses;
    }

    /**
     * update the connections hashmap with the connections in movie
     * @param movie
     */
    public void updateMap(Movie movie) {
    }

    /**
     * adds a correct guess to the set
     */
    public void updateCorrectGuesses() {

    }

    /**
     * adds an incorrect guess to set
     */
    public void updateIncorrectGuesses() {

    }
}
