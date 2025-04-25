import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Player {
    private String username;
    private Map<String, Integer> connections;
    private Set<Movie> correctGuesses;
    private Set<Movie> incorrectGuesses;

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
     *
     * @return the connections map of the player
     */
    public Map<String, Integer> getConnectionsMap() {
        return connections;
    }

    public int getConnectionOfPerson(String person) {
        return connections.get(person);
    }

    /**
     * Sets the connections map of the player
     * @param connections
     */
    public void setConnections(Map<String, Integer> connections) {
        this.connections = connections;
    }

    public void updateConnections(Set<String> connectionsToUpdate) {
        for (String person : connectionsToUpdate) {
            int current = connections.getOrDefault(person, 0);
            connections.put(person, current + 1);
        }
    }

    /**
     *
     * @return the set of correct guesses
     */
    public Set<Movie> getCorrectGuesses() {
        return correctGuesses;
    }

    /**
     * adds a correct guess to the set
     */
    public void updateCorrectGuesses(Movie guess) {
        this.correctGuesses.add(guess);

    }

    /**
     *
     * @return the set of incorrect guesses
     */
    public Set<Movie> getIncorrectGuesses() {
        return incorrectGuesses;
    }


    /**
     * adds an incorrect guess to set
     */
    public void updateIncorrectGuesses(Movie guess) {
        this.incorrectGuesses.add(guess);
    }
}
