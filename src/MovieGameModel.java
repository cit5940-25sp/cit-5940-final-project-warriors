import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MovieGameModel implements IObservable {

    private GameState gameState;
    private List<IObserver> observers;
    private boolean hasChanged;
    private Map<String, Set<String>> moviePeopleMap;
    private Map<String, Integer> connectionsUsed;

    /**
     * Initialize a new MovieGameModel
     */
    public MovieGameModel() {
//        this.gameState = new GameState();
        this.observers = new ArrayList<>();
        this.hasChanged = false;
        moviePeopleMap = new HashMap<>();
        this.connectionsUsed = new HashMap<>();
    }

    public void loadFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            // Read each line from the CSV
            while ((line = br.readLine()) != null) {
                // Skip the header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Split line by first comma (title, people)
                int firstCommaIndex = line.indexOf(',');
                if (firstCommaIndex == -1) continue;

                String title = line.substring(0, firstCommaIndex).trim();
                String peopleRaw = line.substring(firstCommaIndex + 1).trim();

                // Remove quotes from the people string
                if (peopleRaw.startsWith("\"") && peopleRaw.endsWith("\"")) {
                    peopleRaw = peopleRaw.substring(1, peopleRaw.length() - 1);
                }

                // Split people by comma and add them to a set
                String[] peopleArray = peopleRaw.split(",\\s*");
                Set<String> peopleSet = new HashSet<>(Arrays.asList(peopleArray));

                // Add the title and people set to the map
                moviePeopleMap.put(title, peopleSet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getPeopleByTitle(String title) {
        return moviePeopleMap.getOrDefault(title, Collections.emptySet());
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
    public String getConnection(Movie movie1, Movie movie2, String connectionType) {
        if (connectionType == null) {
            return null;
        }

        Set<String> movie1Set;
        Set<String> movie2Set;

        switch (connectionType) {
            case "actor":
                movie1Set = movie1.getActors();
                movie2Set = movie2.getActors();
                break;
            case "director":
                movie1Set = movie1.getDirectors();
                movie2Set = movie2.getDirectors();
                break;
            case "cinematographer":
                movie1Set = movie1.getCinematographers();
                movie2Set = movie2.getCinematographers();
                break;
            case "composer":
                movie1Set = movie1.getComposers();
                movie2Set = movie2.getComposers();
                break;
            case "writer":
                movie1Set = movie1.getWriters();
                movie2Set = movie2.getWriters();
                break;
            default:
                return null;
        }

        for (String name : movie1Set) {
            if (movie2Set.contains(name)) {
                return name;
            }
        }

        return null;
    }

    /**
     * @return TRUE if the connection is valid, FALSE otherwise
     */
    public boolean checkConnection(Movie currentMovie, Movie nextMovie) {
        String connectionType = getConnectionType(currentMovie, nextMovie);

        if (connectionType == null) {
            return false;
        }

        String connection = getConnection(currentMovie, nextMovie, connectionType);
        if (connection == null) {
            return false;
        }

        // check connection hasn't been used 3 or more times
        if (connectionsUsed.getOrDefault(connection, 0) >= 3) {
            return false;
        }

        return true;
    }

}
