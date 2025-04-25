import java.util.*;

public class GameState {

    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Deque<Movie> lastFiveMovies;
    private Map<Movie, Set<String>> lastFiveConnections;
    private Set<Movie> allMovies;

    /**
     * Initialize a new GameState
     * @param username1
     * @param username2
     */
    public GameState(String username1, String username2, Movie startingMovie) {
        this.player1 = new Player(username1);
        this.player2 = new Player(username2);
        this.lastFiveMovies = new ArrayDeque<>(5);
        this.lastFiveMovies.add(startingMovie);
        this.lastFiveConnections = new HashMap<>(5);
        this.lastFiveConnections.put(startingMovie, null);
        this.allMovies = new HashSet<>();
        this.allMovies.add(startingMovie);
        // randomly decide which player is first
        Random rand = new Random();
        int binary = rand.nextInt(2);  // returns 0 or 1
        this.currentPlayer = (binary == 0) ? player1 : player2;
    }

    /**
     *
     * @return a queue of the movies that have been played in order
     */
    public Deque<Movie> getLastFiveMovies() {
        return this.lastFiveMovies;
    }

    /**
     *
     * @return a queue of the movies that have been played in order
     */
    public Map<Movie, Set<String>> getLastFiveConnections() {
        return this.lastFiveConnections;
    }

    /**
     * Returns the most recently added movie
     * @return
     */
    private Movie getMostRecentMovie() {
        return getLastFiveMovies().peekLast();
    }

    public boolean validateGuess(Movie guess) {
        // if guessed before, even if incorrect
        if (allMovies.contains(guess)) {
            currentPlayer.updateIncorrectGuesses(guess);
            return false;
        }
        allMovies.add(guess);
        // check the movie against the most recent movie in Queue
        Set<String> connections = getConnections(guess, getMostRecentMovie());
        // if there are no connections then return false
        if (connections.isEmpty()) {
            currentPlayer.updateIncorrectGuesses(guess);
            return false;
        }
        // if there are connections but the currentPlayer has hit their limit
        Set<String> toUpdate = new HashSet<>();
        for (String person : connections) {
            if (currentPlayer.retrieveConnection(person) >= 3) {
                continue;
            }
            toUpdate.add(person);
        }
        // if all connections are at their limit, return false
        if (toUpdate.isEmpty()) {
            currentPlayer.updateIncorrectGuesses(guess);
            return false;
        }
        // valid guess
        updateGuess(guess, toUpdate);
        return true;
    }

    private void updateGuess(Movie guess, Set<String> connections) {
        // ensure lastFiveMovies and lastFiveConnections doesn't exceed 5
        if (lastFiveMovies.size() == 5) {
            lastFiveMovies.pollFirst();
            lastFiveConnections.remove(guess);
        }
        // add to lastFive
        lastFiveMovies.add(guess);
        lastFiveConnections.put(guess, connections);
        // update currentPlayer info
        currentPlayer.updateConnections(connections); // connections map
        currentPlayer.updateCorrectGuesses(guess); // correct guesses set
        // update currentPlayer to next player
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }


    private Set<String> getConnections(Movie movie1, Movie movie2) {
        Set<String> intersectionSet = movie1.getAllPeople();
        intersectionSet.retainAll(movie2.getAllPeople());
        return intersectionSet;
    }






}
