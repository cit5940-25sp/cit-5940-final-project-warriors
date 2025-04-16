import java.util.*;

public class MovieGameModel implements IObservable {

    private GameState gameState;
    private List<IObserver> observers;
    private boolean hasChanged;
    private Map<String, Integer> connectionsUsed;

    /**
     * Initialize a new MovieGameModel
     */
    public MovieGameModel() {
        this.gameState = new GameState();
        this.observers = new ArrayList<>();
        this.hasChanged = false;
        this.connectionsUsed = new HashMap<>();
    }

    /**
     * @return the current gameState
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Adds a new observer
     */
    @Override
    public void addObserver(IObserver o) {
        observers.add(o);
    }

    /**
     * Removes an observer
     */
    @Override
    public void removeObserver(IObserver o) {
        observers.remove(o);
    }

    /**
     * Clears all observers
     */
    @Override
    public void removeAllObservers() {
        observers.clear();
    }

    /**
     * Update observer status
     */
    @Override
    public void setChanged() {
        hasChanged = true;
    }

    /**
     * Reset an observer
     */
    @Override
    public void clearChanged() {
        hasChanged = false;
    }

    /**
     * @return if an observer has been updated
     */
    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

    /**
     * Updates observers if there has been a change
     */
    @Override
    public void notifyObservers(String event) {
        if (hasChanged) {
            List<IObserver> copy = new ArrayList<>(observers);
            clearChanged();

            for (IObserver observer : copy) {
                observer.update(event);
            }
        }
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

    /**
     * @return the set of shared connection values based on the
     * type of connection. Return NULL if no valid connection.
     */
    public Set<String> getConnection(Movie movie1, Movie movie2, String connectionType) {
        if (connectionType == null) {
            return null;
        }

        switch (connectionType) {
            case "actor":
                Set<String> sharedActors = new HashSet<>(movie1.getActors());
                sharedActors.retainAll(movie2.getActors());
                return sharedActors;
            case "director":
                Set<String> sharedDirectors = new HashSet<>(movie1.getDirectors());
                sharedDirectors.retainAll(movie2.getDirectors());
                return sharedDirectors;
            case "cinematographer":
                Set<String> sharedCinematographers = new HashSet<>(movie1.getCinematographers());
                sharedCinematographers.retainAll(movie2.getCinematographers());
                return sharedCinematographers;
            case "composer":
                Set<String> sharedComposers = new HashSet<>(movie1.getComposers());
                sharedComposers.retainAll(movie2.getComposers());
                return sharedComposers;
            case "writer":
                Set<String> sharedWriters = new HashSet<>(movie1.getWriters());
                sharedWriters.retainAll(movie2.getWriters());
                return sharedWriters;
            default:
                return null;
        }
    }

    /**
     * @return a valid connection from set of connections found
     * from getConnection if it hasn't been used 3 times.
     * Otherwise, returns NULL to signify no valid connections.
     */
    public String chooseConnection(Set<String> connections) {
        if (connections.isEmpty()) {
            return null;
        }

        for (String connection : connections) {
            if (connectionsUsed.containsKey(connection)) {
                // connection has been used less than 3 times, use connection
                // otherwise go to next connection
                if (connectionsUsed.get(connection) < 3) {
                    connectionsUsed.put(connection, connectionsUsed.get(connection) + 1);
                    return connection;
                }
                // add connection to map of used connections
            } else {
                connectionsUsed.put(connection, 1);
                return connection;
            }
        }

        // connection has been used max times
        return null;
    }




}
