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

    public int retrieveConnection(String person) {
        return getConnections().get(person);
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
     * Sets the set of correct guesses
     * @param correctGuesses
     */
    public void setCorrectGuesses(Set<Movie> correctGuesses) {
        this.correctGuesses = correctGuesses;
    }

    public void addToCorrectGuesses(Movie movie) {
        correctGuesses.add(movie);
    }

    /**
     *
     * @return the set of incorrectguesses
     */
    public Set<Movie> getIncorrectGuesses() {
        return incorrectGuesses;
    }

    /**
     * Sets the set of incorrect guesses
     * @param incorrectGuesses
     */
    public void setIncorrectGuesses(Set<Movie> incorrectGuesses) {
        this.incorrectGuesses = incorrectGuesses;
    }

    /**
     * adds a correct guess to the set
     */
    public void updateCorrectGuesses(Movie guess) {
        this.correctGuesses.add(guess);

    }

    /**
     * adds an incorrect guess to set
     */
    public void updateIncorrectGuesses(Movie guess) {
        this.incorrectGuesses.add(guess);
    }
}
