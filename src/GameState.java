import java.util.*;

public class GameState {

    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private int roundsPlayed;
    private Deque<Movie> moviesPlayed;
    private Set<Movie> moviesGuessed;

    /**
     * Initialize a new GameState
     * @param username1
     * @param username2
     */
    public GameState(String username1, String username2) {
        this.player1 = new Player(username1);
        this.player2 = new Player(username2);
        this.roundsPlayed = 0;
        this.moviesPlayed = new ArrayDeque<>();
        this.moviesGuessed = new HashSet<>();
        this.currentPlayer = player1;
    }

    /**
     *
     * @return the number of rounds played
     */
    public int getRoundsPlayed() {
        return this.roundsPlayed;
    }

    /**
     *
     * @return a queue of the movies that have been played in order
     */
    public Deque<Movie> getMoviesPlayed() {
        return this.moviesPlayed;
    }

    /**
     *
     * @return the current player whose turn it is
     */
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * Sets the current player
     * @param player
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    /**
     * Returns the most recently added movie
     * @return
     */
    public Movie getMostRecentMovie() {
        return getMoviesPlayed().peekLast();
    }

    public boolean validateGuess(Movie guess) {
        // return false if guessed before
        if (moviesGuessed.contains(guess)) {
            currentPlayer.updateIncorrectGuesses(guess);
            return false;
        }
        moviesGuessed.add(guess);
        // check the movie against the most recent movie in Queue
        Set<String> connections = getConnections(guess, getMostRecentMovie());
        // if there are no connections then return false
        if (connections.isEmpty()) {
            currentPlayer.updateIncorrectGuesses(guess);
            return false;
        }
        // if there are connections but the currentPlayer has hit their limit
        // then update currentPlayer's incorrectGuesses and return false
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
        moviesPlayed.add(guess); // add movie to played list
        currentPlayer.updateConnections(toUpdate); // update connections
        currentPlayer.updateCorrectGuesses(guess);
        return true;
    }

    private void updateGuess() {
        // add to moviesPlayed queue
        // update currentPlayer's connections map
        // update currentPlayer's correctGuesses
        // update currentPlayer to next player
    }


    public Set<String> getConnections(Movie movie1, Movie movie2) {
        Set<String> intersectionSet = movie1.getAllPeople();
        intersectionSet.retainAll(movie2.getAllPeople());
        return intersectionSet;
    }

    /**
     * @return the type of connection between two movies. Return NULL
     * if no connection is possible.
     */
    public String getConnectionType(Movie movie1, Movie movie2) {

        for (String actor : movie1.getActors()) {
            if (movie2.getActors().contains(actor)) {
                return "actor";
            }
        }

        for (String director : movie1.getDirectors()) {
            if (movie2.getDirectors().contains(director)) {
                return "director";
            }
        }

        for (String cinematographer : movie1.getCinematographers()) {
            if (movie2.getCinematographers().contains(cinematographer)) {
                return "cinematographer";
            }
        }

        for (String composer : movie1.getComposers()) {
            if (movie2.getComposers().contains(composer)) {
                return "composer";
            }
        }

        for (String writer : movie1.getWriters()) {
            if (movie2.getWriters().contains(writer)) {
                return "writer";
            }
        }

        return null;
    }

//    /**
//     * @return the set of shared connection values based on the
//     * type of connection. Return NULL if no valid connection.
//     */
//    public Set<String> getConnection(Movie movie1, Movie movie2, String connectionType) {
//        if (connectionType == null) {
//            return null;
//        }
//
//        switch (connectionType) {
//            case "actor":
//                Set<String> sharedActors = new HashSet<>(movie1.getActors());
//                sharedActors.retainAll(movie2.getActors());
//                return sharedActors;
//            case "director":
//                Set<String> sharedDirectors = new HashSet<>(movie1.getDirectors());
//                sharedDirectors.retainAll(movie2.getDirectors());
//                return sharedDirectors;
//            case "cinematographer":
//                Set<String> sharedCinematographers = new HashSet<>(movie1.getCinematographers());
//                sharedCinematographers.retainAll(movie2.getCinematographers());
//                return sharedCinematographers;
//            case "composer":
//                Set<String> sharedComposers = new HashSet<>(movie1.getComposers());
//                sharedComposers.retainAll(movie2.getComposers());
//                return sharedComposers;
//            case "writer":
//                Set<String> sharedWriters = new HashSet<>(movie1.getWriters());
//                sharedWriters.retainAll(movie2.getWriters());
//                return sharedWriters;
//            default:
//                return null;
//        }
//    }

//    /**
//     * @return a valid connection from set of connections found
//     * from getConnection if it hasn't been used 3 times.
//     * Otherwise, returns NULL to signify no valid connections.
//     */
//    public String chooseConnection(Set<String> connections) {
//        if (connections.isEmpty()) {
//            return null;
//        }
//
//        for (String connection : connections) {
//            if (connectionsUsed.containsKey(connection)) {
//                // connection has been used less than 3 times, use connection
//                // otherwise go to next connection
//                if (connectionsUsed.get(connection) < 3) {
//                    connectionsUsed.put(connection, connectionsUsed.get(connection) + 1);
//                    return connection;
//                }
//                // add connection to map of used connections
//            } else {
//                connectionsUsed.put(connection, 1);
//                return connection;
//            }
//        }
//
//        // connection has been used max times
//        return null;
//    }






}
