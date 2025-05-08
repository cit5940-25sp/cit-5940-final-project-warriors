import java.util.*;

/**
 * The MovieGameModel class has the core game logic for a two-player movie connection game.
 * It handles state management, turn handling, guess validation, and observer updates.
 * This model follows the Observer pattern and interacts with Movie and Player objects.
 */
public class MovieGameModel implements IObservable {
    private String player1Name = "";
    private String player2Name = "";
    private boolean enteringPlayer1 = true;
    private boolean gameOver = false;

    private Player player1 = new Player();
    private Player player2 = new Player();
    private Player currentPlayer;
    private Deque<Movie> lastFiveMovies = new ArrayDeque<>(5);
    private Map<String, Set<String>> lastFiveConnections = new HashMap<>(5);
    private Map<String, Player> lastFivePlayers = new HashMap<>(5);
    private Set<Movie> allMovies = new HashSet<>();
    private List<IObserver> observers = new ArrayList<>();
    private boolean hasChanged = false;

    private List<String> suggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private List<String> suggestionGenres = new ArrayList<>();
    ArrayList<String> dictionary;

    private int secondsRemaining = 30;

    private boolean gameStarted = false;
    private int roundNumber = 0;
    private String selectedGenre = "";
    private int selectedGenreIndex = 0;
    private boolean selectingGenre = false;
    private final String[] genreList = {
            "Action", "Adventure", "Animation", "Comedy",
            "Crime", "Drama", "Family", "Fantasy",
            "Horror", "Romance", "Sci-Fi", "Thriller"
    };


    // Power-up system
    private int player1TimeBoosts = 2;
    private int player2TimeBoosts = 2;
    private int player1TimeSabotages = 1;
    private int player2TimeSabotages = 1;
    private boolean nextPlayerSabotaged = false;

    /**
     * Constructs a MovieGameModel with an initial movie and a set of all possible movie names.
     * Initializes player state and the internal dictionary for autocomplete suggestions.
     *
     * @param startingMovie The first movie to be added to the game.
     * @param movieNames A set of all movie titles to be used for suggestions.
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

    /**
     * @return Map of movie titles to the player who submitted them.
     */
    public Map<String, Player> getLastFivePlayers() {
        return this.lastFivePlayers;
    }

    /**
     * @return true if it's Player 1's turn.
     */
    public boolean isPlayer1Turn() {
        return (roundNumber % 2 == 0);
    }

    /**
     * @return true if currently entering Player 1's name.
     */
    public boolean getEnteringPlayer1() {
        return this.enteringPlayer1;
    }

    /**
     * @return the name entered for Player 1.
     */
    public String getPlayer1Name() {
        return this.player1Name;
    }

    /**
     * @return the name entered for Player 2.
     */
    public String getPlayer2Name() {
        return this.player2Name;
    }

    /**
     * Sets whether the current name being entered is for Player 1.
     * @param bool true if entering name for Player 1.
     */
    public void setEnteringPlayer1(boolean bool) {
        this.enteringPlayer1 = bool;
    }

    /**
     * Appends a character to the currently active player name being entered.
     * @param charc the character to append.
     */
    public void incrementPlayerNames(char charc) {
        if (enteringPlayer1) {
            player1Name += charc;
        } else {
            player2Name += charc;
        }
    }

    /**
     * Removes the last character from the current player name being entered.
     */
    public void decrementPlayerNames() {
        if (enteringPlayer1 && !player1Name.isEmpty()) {
            player1Name = player1Name.substring(0, player1Name.length() - 1);
        } else if (!enteringPlayer1 && !player2Name.isEmpty()) {
            player2Name = player2Name.substring(0, player2Name.length() - 1);
        }
    }

    /**
     * Assigns the finalized names to each player.
     */
    public void initializePlayerNames() {
        player1.setUsername(player1Name);
        player2.setUsername(player2Name);
    }

    /**
     * Moves suggestion index up in the suggestion list.
     */
    public void moveSuggestionUp() {
        if (!getSuggestions().isEmpty()) {
            setSuggestionIndex((getSuggestionIndex() - 1 + getSuggestions().size()) % getSuggestions().size());
        }
    }

    /**
     * Moves suggestion index down in the suggestion list.
     */
    public void moveSuggestionDown() {
        if (!getSuggestions().isEmpty()) {
            setSuggestionIndex((getSuggestionIndex() + 1) % getSuggestions().size());
        }
    }

    /**
     * Moves suggestion index to the left column.
     */
    public void moveSuggestionLeft() {
        if (!getSuggestions().isEmpty() && getSuggestionIndex() >= 2) {
            // Move from right column to left column
            setSuggestionIndex(getSuggestionIndex() % 2);
        }
    }

    /**
     * Moves suggestion index to the right column.
     */
    public void moveSuggestionRight() {
        if (!getSuggestions().isEmpty() && getSuggestionIndex() < 2) {
            // Move from left column to right column
            setSuggestionIndex(Math.min(getSuggestionIndex() + 2, getSuggestions().size() - 1));
        }
    }

    /**
     * Updates the list of autocomplete suggestions based on the current input prefix.
     * @param currentInput a StringBuilder representing the user's current input.
     */
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
     * @return a queue of the movies that have been played in order.
     */
    public Deque<Movie> getLastFiveMovies() {
        return this.lastFiveMovies;
    }

    /**
     * @return a queue of the movies that have been played in order.
     */
    public Map<String, Set<String>> getLastFiveConnections() {
        return this.lastFiveConnections;
    }

    /**
     * @return the most recently added movie.
     */
    private Movie getMostRecentMovie() {
        return getLastFiveMovies().peekLast();
    }

    /**
     * Validates a guessed movie based on connection rules and updates state if valid.
     * @param guess The Movie being guessed.
     * @return true if the guess was valid and accepted.
     */
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

    /**
     * Updates the game state based on the player's guess.
     *
     * @param guess       the {@link Movie} object representing the player's guess
     * @param connections a set of shared people (actors, directors, etc.) between the guessed movie and the previous movie
     */
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
        if (guess.getGenres().contains(selectedGenre)) {
            currentPlayer.incrementScore();
            if (currentPlayer.getScore() >= 5) {
                setChanged();
                notifyObservers("GAME_OVER_" + (isPlayer1Turn()? "2" : "1"));
                return;
            }

        }
        currentPlayer.updateConnections(connections); // connections map
        currentPlayer.updateCorrectGuesses(guess); // correct guesses set
        // update currentPlayer to next player
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Retrieves the set of people common to both movies.
     *
     * @param movie1 the first {@link Movie} object
     * @param movie2 the second {@link Movie} object
     * @return a set of names that appear in both movies' contributor lists
     */
    private Set<String> getConnections(Movie movie1, Movie movie2) {
        Set<String> intersectionSet = movie1.getAllPeople();
        intersectionSet.retainAll(movie2.getAllPeople());
        return intersectionSet;
    }

    /**
     * Adds a new observer.
     */
    @Override
    public void addObserver(IObserver o) {
        observers.add(o);
    }

    /**
     * Removes an observer.
     */
    @Override
    public void removeObserver(IObserver o) {
        observers.remove(o);
    }

    /**
     * Clears all observers.
     */
    @Override
    public void removeAllObservers() {
        observers.clear();
    }

    /**
     * Update observer status.
     */
    @Override
    public void setChanged() {
        hasChanged = true;
    }

    /**
     * Reset an observer.
     */
    @Override
    public void clearChanged() {
        hasChanged = false;
    }

    /**
     * @return true if an observer has been updated.
     */
    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

    /**
     * Notifies all registered observers of a specific game event.
     * @param event A string representing the event.
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
     * @return List of current autocomplete suggestions.
     */
    public List<String> getSuggestions() {
        return suggestions;
    }

    /**
     * @return The current suggestion index.
     */
    public int getSuggestionIndex() {
        return suggestionIndex;
    }

    /**
     * Sets the current suggestion index.
     * @param suggestionIndex The index to set.
     */
    public void setSuggestionIndex(int suggestionIndex) {
        this.suggestionIndex = suggestionIndex;
    }

    /**
     * @return Remaining time in seconds for current round.
     */
    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    /**
     * Decrements the timer by one second.
     */
    public void decrementSecondsRemaining() {
        this.secondsRemaining--;
    }

    /**
     * @return true if the game has officially started.
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Sets whether the game has started.
     * @param gameStarted true to indicate game start.
     */
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    /**
     * @return the current round number.
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * @return Player 1's current score.
     */
    public int getPlayer1Score() {
        return player1.getScore();
    }

    /**
     * @return Player 2's current score.
     */
    public int getPlayer2Score() {
        return player2.getScore();
    }

    /**
     * @return Remaining time boosts for Player 1.
     */
    public int getPlayer1TimeBoosts() {
        return player1TimeBoosts;
    }

    /**
     * @return Remaining time boosts for Player 2.
     */
    public int getPlayer2TimeBoosts() {
        return player2TimeBoosts;
    }

    /**
     * @return Remaining time sabotages for Player 1.
     */
    public int getPlayer1TimeSabotages() {
        return player1TimeSabotages;
    }

    /**
     * Sets Player 1's time sabotages.
     * @param player1TimeSabotages number of sabotages left.
     */
    public void setPlayer1TimeSabotages(int player1TimeSabotages) {
        this.player1TimeSabotages = player1TimeSabotages;
    }

    /**
     * @return Remaining time sabotages for Player 2.
     */
    public int getPlayer2TimeSabotages() {
        return player2TimeSabotages;
    }

    /**
     * Sets Player 2's time sabotages.
     * @param player2TimeSabotages number of sabotages left.
     */
    public void setPlayer2TimeSabotages(int player2TimeSabotages) {
        this.player2TimeSabotages = player2TimeSabotages;
    }

    /**
     * Applies sabotage effect to the next player by reducing their available time.
     * @param nextPlayerSabotaged true if next player is sabotaged.
     */
    public void setNextPlayerSabotaged(boolean nextPlayerSabotaged) {
        this.nextPlayerSabotaged = nextPlayerSabotaged;
    }

    /**
     * Prepares the game state for the next round and adjusts timers accordingly.
     */
    public void updateToNextRound() {
        System.out.println("Next player sabotaged: " + nextPlayerSabotaged);

        secondsRemaining = nextPlayerSabotaged ? 20 : 30;
        nextPlayerSabotaged = false;

        System.out.println("Seconds remaining after sabotage: " + secondsRemaining);

        roundNumber++;
        suggestions.clear();
        suggestionGenres.clear();
    }

    /**
     * Adds 15 seconds to the current player's timer and reduces their remaining time boosts by one.
     */
    public void updateTimeBoosts() {
        secondsRemaining += 15;
        if (isPlayer1Turn()) {
            player1TimeBoosts--;
        } else {
            player2TimeBoosts--;
        }
    }

    /**
     * Starts a new game session by initializing player names and notifying observers of the game start event.
     */
    public void startNewGame() {
        setGameStarted(true);
        initializePlayerNames();
        setChanged();
        notifyObservers("GAME_START");
        selectedGenre = genreList[selectedGenreIndex];
        selectingGenre = false;
    }

    /**
     * Resets the game model to its initial state using a given starting movie.
     *
     * @param startingMovie the movie to initialize the game with.
     */
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

    /**
     * @return the currently selected genre.
     */
    public String getSelectedGenre() {
        return selectedGenre;
    }

    /**
     * @return the index of the currently selected genre in the genre list.
     */
    public int getSelectedGenreIndex() {
        return selectedGenreIndex;
    }

    /**
     * Updates the selected genre index by a delta value, cycling through available genres.
     *
     * @param delta       the amount to change the index by.
     * @param genresCount the total number of genres in the list.
     */
    public void selectNextGenre(int delta, int genresCount) {
        selectedGenreIndex = (selectedGenreIndex + delta + genresCount) % genresCount;
        selectedGenre = genreList[selectedGenreIndex];
    }

    /**
     * @return true if the game is in the genre selection phase.
     */
    public boolean isSelectingGenre() {
        return selectingGenre;
    }

    /**
     * Sets whether the game is currently in the genre selection phase.
     *
     * @param selectingGenre true to indicate genre selection is in progress.
     */
    public void setSelectingGenre(boolean selectingGenre) {
        this.selectingGenre = selectingGenre;
    }

    /**
     * @return the full list of available genres.
     */
    public String[] getGenreList() {
        return genreList;
    }

    /**
     * @return true if the game has ended.
     */
    public boolean getGameOver() {
        return gameOver;
    }
}
