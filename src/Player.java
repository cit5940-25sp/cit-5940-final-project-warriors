import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a player and their stats for the Movie Game.
 */
public class Player {
    private String username = "";
    private int score = 0;
    private Map<String, Integer> connections = new HashMap<>();;
    private Set<Movie> correctGuesses = new HashSet<>();;
    private Set<Movie> incorrectGuesses = new HashSet<>();

    /**
     * Initialize a new Player
     */
    public Player() {};

    /**
     * Initialize a new Player
     *
     * @param username The username of the player
     */
    public Player(String username) {
        this.username = username;
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
    public void setUsername(String username) {this.username = username;}


    public int getConnectionOfPerson(String person) {
        return connections.getOrDefault(person, 0);
    }

    /**
     * Sets the connections map of the player
     * @param connections Map of connections
     */
    public void setConnections(Map<String, Integer> connections) {
        this.connections = connections;
    }

    /**
     * Updates the connections map of the player
     * @param connectionsToUpdate Set of connections to update
     */
    public void updateConnections(Set<String> connectionsToUpdate) {
        for (String person : connectionsToUpdate) {
            int current = connections.getOrDefault(person, 0);
            connections.put(person, current + 1);
        }
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

    /**
     * @return the score
     */
    public int getScore() {
        return score;
    }

    public Map<String, Integer> getConnections() {
        return connections;
    }
    /**
     * sets the score field
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * increments the score
     */
    public void incrementScore() {
        this.score++;
    }

    /**
     * resets the score, guesses, and connections
     */
    public void reset() {
        score = 0;
        username = "";
        correctGuesses.clear();
        incorrectGuesses.clear();
        connections.clear();
    }
}
