import java.util.*;

public class MovieGameModel implements IObservable {

    private String player1Name = "";
    private String player2Name = "";
    private boolean enteringPlayer1 = true;

    private Player player1 = new Player();
    private Player player2 = new Player();
    private Player currentPlayer;
    private Deque<Movie> lastFiveMovies = new ArrayDeque<>(5);
    private Map<Movie, Set<String>> lastFiveConnections = new HashMap<>(5);
    private Set<Movie> allMovies = new HashSet<>();
    private List<IObserver> observers = new ArrayList<>();
    private boolean hasChanged;

    private List<String> suggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private List<String> suggestionGenres = new ArrayList<>();
    List<String> dictionary = new ArrayList<>();

    private Deque<String> movieHistory = new ArrayDeque<>();
    private Deque<String> movieGenres = new ArrayDeque<>();
    private List<String> connections = new ArrayList<>();
    private List<Boolean> connectionOwners = new ArrayList<>(); // true for Player 1, false for Player 2

    private int secondsRemaining = 30;

    private boolean gameStarted = false;
    private int roundNumber = 0;

    private int player1Score = 0;
    private int player2Score = 0;

    // Power-up system
    private int player1TimeBoosts = 2;  // Player 1 has 2 time boost power-ups
    private int player2TimeBoosts = 2;  // Player 2 has 2 time boost power-ups
    private int player1TimeSabotages = 1;  // Player 1 has 1 time sabotage power-up
    private int player2TimeSabotages = 1;  // Player 2 has 1 time sabotage power-up


    /**
     * Initialize a new GameState
     */
    public MovieGameModel(Movie startingMovie, Set<String> movieNames) {
        // initialize state with starting movie
        this.lastFiveMovies.add(startingMovie);
        this.lastFiveConnections.put(startingMovie, null);
        this.allMovies.add(startingMovie);

        // randomly starting player
        Random rand = new Random();
        int binary = rand.nextInt(2);  // returns 0 or 1
        this.currentPlayer = (binary == 0) ? player1 : player2;

        // create dictionary of movie titles
        List<String> titles = new ArrayList<>(movieNames);
        this.dictionary = new ArrayList<>();
        for (String movieName : titles) {
            movieName = movieName.toLowerCase();
            this.dictionary.add(movieName);
        }
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

    public void updatePlayer1Name(char charc) {
        this.player1Name += charc;
    }

    public void updatePlayer2Name(char charc) {
        this.player2Name += charc;
    }

    public void setPlayer1Name(String name) {
        this.player1Name = name;
    }

    public void setPlayer2Name(String name) {
        this.player2Name = name;
    }


    public void setEnteringPlayer1(boolean bool) {
        this.enteringPlayer1 = bool;
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

    public void clearSuggestions() {
        this.suggestions.clear();
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
            if (currentPlayer.getConnectionOfPerson(person) >= 3) {
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

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
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

    public void setSecondsRemaining(int secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
    }

    public void decrementSecondsRemaining() {
        this.secondsRemaining--;
    }

    public void updateSecondsRemaining(int offset) {
        this.secondsRemaining += offset;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public Deque<String> getMovieHistory() {
        return movieHistory;
    }

    public void setMovieHistory(Deque<String> movieHistory) {
        this.movieHistory = movieHistory;
    }

    public void updateMovieHistory(String selectedTitle) {
        movieHistory.addFirst(selectedTitle);
        if (movieHistory.size() > 5) {
            movieHistory.removeLast();
        }
    }

    public void clearMovieHistory() {
        this.movieHistory.clear();
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public void incrementRoundNumber() {
        this.roundNumber++;
    }

    public List<String> getSuggestionGenres() {
        return suggestionGenres;
    }

    public void setSuggestionGenres(List<String> suggestionGenres) {
        this.suggestionGenres = suggestionGenres;
    }

    public void clearSuggestionGenres() {
        this.suggestionGenres.clear();
    }

    public Deque<String> getMovieGenres() {
        return movieGenres;
    }

    public void setMovieGenres(Deque<String> movieGenres) {
        this.movieGenres = movieGenres;
    }

    public void clearMovieGenres() {
        this.movieGenres.clear();
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }

    public void clearConnections() {
        this.connections.clear();
    }

    public void addToConnections(int index, String phrase) {
        this.connections.add(index, phrase);
    }

    public List<Boolean> getConnectionOwners() {
        return connectionOwners;
    }

    public void setConnectionOwners(List<Boolean> connectionOwners) {
        this.connectionOwners = connectionOwners;
    }

    public void addToConnectionOwners(int index, boolean bool) {
        this.connectionOwners.add(index, bool);
    }

    public void clearConnectionOwners() {
        this.connectionOwners.clear();
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public void incrementPlayer1Score() {
        this.player1Score++;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public void incrementPlayer2Score() {
        this.player2Score++;
    }

    public void updateMovieGenres(String genre) {
        this.movieGenres.addFirst(genre);
    }

    public int getPlayer1TimeBoosts() {
        return player1TimeBoosts;
    }

    public void setPlayer1TimeBoosts(int player1TimeBoosts) {
        this.player1TimeBoosts = player1TimeBoosts;
    }

    public void decrementPlayer1TimeBoosts() {
        this.player1TimeBoosts--;
    }

    public int getPlayer2TimeBoosts() {
        return player2TimeBoosts;
    }

    public void setPlayer2TimeBoosts(int player2TimeBoosts) {
        this.player2TimeBoosts = player2TimeBoosts;
    }

    public void decrementPlayer2TimeBoosts() {
        this.player2TimeBoosts--;
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

    public void rotate() {
        if (movieHistory.size() > 5) {
            movieHistory.removeLast();
            movieGenres.removeLast();
            if (connections.size() > 5 - 1) {
                connections.removeLast();
                connectionOwners.removeLast();
            }
        }
    }

    public void checkTimeSabotage() {
        boolean nextPlayerIsSabotaged = false;
        if (roundNumber % 2 == 0) {
            if (player1TimeSabotages < 0) {
                nextPlayerIsSabotaged = true;
                player1TimeSabotages++;
            }
        } else {
            if (player2TimeSabotages < 0) {
                nextPlayerIsSabotaged = true;
                player2TimeSabotages++;
            }
        }
    }

    public void resetModel() {
        movieHistory.clear();
        movieGenres.clear();
        connections.clear();
        connectionOwners.clear();
        suggestions.clear();
        suggestionGenres.clear();
        roundNumber = 0;
        secondsRemaining = 30;
        gameStarted = false;
        enteringPlayer1 = true;
        player1Name = "";
        player2Name = "";
        player1Score = 0;
        player2Score = 0;
    }
}
