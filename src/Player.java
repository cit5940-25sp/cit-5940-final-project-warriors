import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Player {
    private String username;
    private int score = 0;
    private Map<String, Integer> connections = new HashMap<>();;
    private Set<Movie> correctGuesses = new HashSet<>();;
    private Set<Movie> incorrectGuesses = new HashSet<>();

    /**
     * Initialize a new Player
     */
    public Player() {};

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

    /**
     *
     * @return the connections map of the player
     */
    public Map<String, Integer> getConnectionsMap() {
        return connections;
    }

    public int getConnectionOfPerson(String person) {
        return connections.getOrDefault(person, 0);
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

    public boolean isPlayer(String name) {
        return username.equals(name);
    }


    /**
     * adds an incorrect guess to set
     */
    public void updateIncorrectGuesses(Movie guess) {
        this.incorrectGuesses.add(guess);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void incrementScore() {
        this.score++;
    }

    public void reset() {
        score = 0;
        correctGuesses.clear();
        incorrectGuesses.clear();
        connections.clear();
    }
}
