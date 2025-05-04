import java.lang.reflect.Array;
import java.util.*;

public class MovieGameModel implements IObservable {

    private String player1Name = "";
    private String player2Name = "";
    private boolean enteringPlayer1 = true;

    private Player player1 = new Player();
    private Player player2 = new Player();
    private Player currentPlayer;
    private Deque<Movie> lastFiveMovies = new ArrayDeque<>(5);
    private Map<String, Set<String>> lastFiveConnections = new HashMap<>(5);
    private Map<String, Player> lastFivePlayers = new HashMap<>(5);
    private Set<Movie> allMovies = new HashSet<>();
    private List<IObserver> observers = new ArrayList<>();
    private boolean hasChanged;

    private List<String> suggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private List<String> suggestionGenres = new ArrayList<>();
    ArrayList<String> dictionary = new ArrayList<>();

    private int secondsRemaining = 30;

    private boolean gameStarted = false;
    private int roundNumber = 0;

    // Power-up system
    private int player1TimeBoosts = 2;  // Player 1 has 2 time boost power-ups
    private int player2TimeBoosts = 2;  // Player 2 has 2 time boost power-ups
    private int player1TimeSabotages = 1;  // Player 1 has 1 time sabotage power-up
    private int player2TimeSabotages = 1;  // Player 2 has 1 time sabotage power-up
    private boolean nextPlayerSabotaged = false;


    /**
     * Initialize a new GameState
     */
    public MovieGameModel(Movie startingMovie, Set<String> movieNames) {
        // initialize state with starting movie
        this.lastFiveMovies.add(startingMovie);
        this.lastFiveConnections.put(startingMovie.getTitle(), null);
        this.lastFivePlayers.put(startingMovie.getTitle(), null);
        this.allMovies.add(startingMovie);
        this.currentPlayer = player1;

        // create dictionary of movie titles
        List<String> titles = new ArrayList<>(movieNames);
        this.dictionary = new ArrayList<>();
        for (String movieName : titles) {
            movieName = movieName.toLowerCase();
            this.dictionary.add(movieName);
        }
    }

    public Map<String, Player> getLastFivePlayers() {
        return this.lastFivePlayers;
    }

    public boolean isPlayer1Turn() {
        return (roundNumber % 2 == 0);
    }

    public boolean getEnteringPlayer1() {
        return this.enteringPlayer1;
    }

    public String getPlayer1Name() {
        return this.player1Name;
    }

    public String getPlayer2Name() {
        return this.player2Name;
    }

    public void setEnteringPlayer1(boolean bool) {
        this.enteringPlayer1 = bool;
    }

    public void incrementPlayerNames(char charc) {
        if (enteringPlayer1) {
            player1Name += charc;
        } else {
            player2Name += charc;
        }
    }

    public void decrementPlayerNames() {
        if (enteringPlayer1 && !player1Name.isEmpty()) {
            player1Name = player1Name.substring(0, player1Name.length() - 1);
        } else if (!enteringPlayer1 && !player2Name.isEmpty()) {
            player2Name = player2Name.substring(0, player2Name.length() - 1);
        }
    }

    public void initializePlayerNames() {
        player1.setUsername(player1Name);
        player2.setUsername(player2Name);
    }

    public void moveSuggestionUp() {
        if (!getSuggestions().isEmpty()) {
            setSuggestionIndex((getSuggestionIndex() - 1 + getSuggestions().size()) % getSuggestions().size());
        }
    }

    public void moveSuggestionDown() {
        if (!getSuggestions().isEmpty()) {
            setSuggestionIndex((getSuggestionIndex() + 1) % getSuggestions().size());
        }
    }

    public void moveSuggestionLeft() {
        if (!getSuggestions().isEmpty() && getSuggestionIndex() >= 2) {
            // Move from right column to left column
            setSuggestionIndex(getSuggestionIndex() % 2);
        }
    }

    public void moveSuggestionRight() {
        if (!getSuggestions().isEmpty() && getSuggestionIndex() < 2) {
            // Move from left column to right column
            setSuggestionIndex(Math.min(getSuggestionIndex() + 2, getSuggestions().size() - 1));
        }
    }

    public void updateSuggestions(StringBuilder currentInput) {
        suggestions.clear();
        suggestionGenres.clear();
        String prefix = currentInput.toString();
        if (!prefix.isEmpty()) {
            int count = 0;
            for (String word : dictionary) {
                if (word.startsWith(prefix.toLowerCase()) && count < 5) {
                    suggestions.add(word);
                    String genre = "genre";
                    suggestionGenres.add(genre);
                    count++;
                }
            }
        }
        suggestionIndex = 0;
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
    public Map<String, Set<String>> getLastFiveConnections() {
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
            System.out.println("Movie already guessed.");
            return false;
        }
        allMovies.add(guess);
        // check the movie against the most recent movie in Queue
        Movie previousMovie = getMostRecentMovie();
        System.out.println("Comparing " + guess.getTitle() + " to " + previousMovie.getTitle());
        Set<String> connections = getConnections(guess, previousMovie);
        // if there are no connections then return false
        if (connections.isEmpty()) {
            currentPlayer.updateIncorrectGuesses(guess);
            System.out.println("No connections found.");
            return false;
        }
        // if there are connections but the currentPlayer has hit their limit
        Set<String> toUpdate = new HashSet<>();
        for (String person : connections) {
            if (currentPlayer.getConnectionOfPerson(person) >= 3) {
                System.out.println("Hit person limit for: " + person);
                continue;
            }
            toUpdate.add(person);
        }
        // if all connections are at their limit, return false
        if (toUpdate.isEmpty()) {
            currentPlayer.updateIncorrectGuesses(guess);
            System.out.println("No connections to update.");
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
            lastFiveConnections.remove(guess.getTitle());
        }
        // add to lastFive
        System.out.println("Added: " + guess.getTitle());
        for (String con : connections) {
            System.out.println(con);
        }
        System.out.println();
        lastFiveMovies.add(guess);
        lastFiveConnections.put(guess.getTitle(), connections);
        lastFivePlayers.put(guess.getTitle(), currentPlayer);
        // update currentPlayer info
        currentPlayer.incrementScore();
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

    public List<String> getSuggestions() {
        return suggestions;
    }

    public int getSuggestionIndex() {
        return suggestionIndex;
    }

    public void setSuggestionIndex(int suggestionIndex) {
        this.suggestionIndex = suggestionIndex;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public void decrementSecondsRemaining() {
        this.secondsRemaining--;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getPlayer1Score() {
        return player1.getScore();
    }

    public int getPlayer2Score() {
        return player2.getScore();
    }

    public int getPlayer1TimeBoosts() {
        return player1TimeBoosts;
    }

    public int getPlayer2TimeBoosts() {
        return player2TimeBoosts;
    }

    public int getPlayer1TimeSabotages() {
        return player1TimeSabotages;
    }

    public void setPlayer1TimeSabotages(int player1TimeSabotages) {
        this.player1TimeSabotages = player1TimeSabotages;
    }

    public int getPlayer2TimeSabotages() {
        return player2TimeSabotages;
    }

    public void setPlayer2TimeSabotages(int player2TimeSabotages) {
        this.player2TimeSabotages = player2TimeSabotages;
    }

    public void updateToNextRound() {
        // Debugging output: Log sabotage state
        System.out.println("Next player sabotaged: " + nextPlayerSabotaged);

        secondsRemaining = nextPlayerSabotaged ? 20 : 30;
        nextPlayerSabotaged = false;

        // Debugging output: Log the seconds remaining
        System.out.println("Seconds remaining after sabotage: " + secondsRemaining);

        roundNumber++;
        suggestions.clear();
        suggestionGenres.clear();
    }

    public void updateTimeBoosts() {
        secondsRemaining += 15;
        if (isPlayer1Turn()) {
            player1TimeBoosts--;
        } else {
            player2TimeBoosts--;
        }
    }

    public void resetModel(Movie startingMovie) {
        lastFiveConnections.clear();
        lastFivePlayers.clear();
        lastFiveMovies.clear();
        suggestions.clear();
        suggestionGenres.clear();
        allMovies.clear();


        roundNumber = 0;
        secondsRemaining = 30;
        gameStarted = false;
        enteringPlayer1 = true;
        player1Name = "";
        player2Name = "";

        player1TimeBoosts = 2;
        player2TimeBoosts = 2;
        player1TimeSabotages = 1;
        player2TimeSabotages = 1;

        player1.reset();
        player2.reset();
        currentPlayer = player1;

        lastFiveMovies.add(startingMovie);
        lastFiveConnections.put(startingMovie.getTitle(), null);
        lastFivePlayers.put(startingMovie.getTitle(), null);
        allMovies.add(startingMovie);
    }

    public void setNextPlayerSabotaged(boolean nextPlayerSabotaged) {
        this.nextPlayerSabotaged = nextPlayerSabotaged;
    }
}
