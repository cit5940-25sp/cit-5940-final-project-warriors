import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller class for the Movie Name Game. Handles input, game state transitions, and
 * coordinates updates between the model and view in an MVC architecture.
 * Implements the IObserver interface to respond to events.
 */
public class MovieGameController implements IObserver{
    private MovieGameModel model;
    private MovieGameView view;
    private Database database = new Database();

    private StringBuilder currentInput = new StringBuilder();
    private boolean timerRunning = true;
    private ScheduledExecutorService scheduler;
    private String selectedTitle = "";
    private boolean shouldExit = false;

    /**
     * Constructs a new MovieGameController, initializes the database, model, and view,
     * sets up the game timer, and registers the view as an observer.
     *
     * @throws IOException If there is an error loading the movie database or updating the view.
     */
    public MovieGameController() throws IOException {
        // initialize database, model, and view
        this.database.loadFromCSV("cleaned_imdb_final.csv");
        this.model = new MovieGameModel(database.getRandomMovie(), database.getMovieNameSet());
        this.view = new MovieGameView(this.model);
        this.model.addObserver(this);

        // schedule timer
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                if (timerRunning && model.getSecondsRemaining() > 0 && model.isGameStarted()) {
                    model.decrementSecondsRemaining();
                    try {
                        view.updateScreen(this.currentInput);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (model.getSecondsRemaining() == 0 && model.isGameStarted()) {
                    model.setChanged();
                    model.notifyObservers("GAME_OVER_" + (model.isPlayer1Turn() ? "2" : "1"));
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Main game loop that handles user input and dispatches actions accordingly.
     * Continuously polls for keyboard input and updates the model and view.
     *
     * @throws IOException if screen polling or rendering encounters an error.
     */
    public void run() throws IOException {
        view.resetView();
        boolean running = true;
        while (running && !shouldExit) {
            KeyStroke keyStroke = view.screenPollInput();
            if (shouldExit) {
                break;
            }
            if (keyStroke != null) {
                // game not started - show landing page
                if (!model.isGameStarted()) {
                    handleLandingInput(keyStroke);
                    continue;
                }

                switch (keyStroke.getKeyType()) {
                    case ArrowUp:
                        model.moveSuggestionUp();
                        model.notifyObservers("REFRESH");
                        break;
                    case ArrowDown:
                        model.moveSuggestionDown();
                        model.notifyObservers("REFRESH");
                        break;
                    case ArrowLeft:
                        model.moveSuggestionLeft();
                        model.notifyObservers("REFRESH");
                        break;
                    case ArrowRight:
                        model.moveSuggestionRight();
                        model.notifyObservers("REFRESH");
                        break;
                    case Character:
                        handleCharacter(Character.toLowerCase(keyStroke.getCharacter()));
                        break;
                    case Backspace:
                        handleBackspace();
                        break;
                    case Enter:
                        handleEnter();
                        break;
                    case EOF:
                    case Escape:
                        running = false;
                        break;
                    default:
                        break;
                }
                view.updateScreen(currentInput);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        cleanup();
    }

    /**
     * Cleans up resources like the scheduler and view at the end of the game.
     *
     * @throws IOException If there is an error cleaning up the view.
     */
    private void cleanup() throws IOException {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        view.cleanUp();
    }

    /**
     * Handles user input during the landing screen phase.
     *
     * @param keyStroke The input key pressed by the user.
     * @throws IOException If there is an error updating the view.
     */
    private void handleLandingInput(KeyStroke keyStroke) throws IOException {
        if (!model.isSelectingGenre()) {
            // player name entry phase
            if (keyStroke.getKeyType() == KeyType.Character) {
                model.incrementPlayerNames(keyStroke.getCharacter());
            } else if (keyStroke.getKeyType() == KeyType.Backspace) {
                model.decrementPlayerNames();
            } else if (keyStroke.getKeyType() == KeyType.Enter) {
                if (model.getEnteringPlayer1() && !model.getPlayer1Name().isEmpty()) {
                    model.setEnteringPlayer1(false);
                } else if (!model.getEnteringPlayer1() && !model.getPlayer2Name().isEmpty()) {
                    model.setSelectingGenre(true);
                }
            }
        } else {
            // genre selection phase
            switch (keyStroke.getKeyType()) {
                case ArrowLeft -> model.selectNextGenre(-1, model.getGenreList().length);
                case ArrowRight -> model.selectNextGenre(1, model.getGenreList().length);
                case ArrowUp -> model.selectNextGenre(-4, model.getGenreList().length);
                case ArrowDown -> model.selectNextGenre(4, model.getGenreList().length);
                case Enter -> {
                    // start game
                    model.startNewGame();
                    view.setCursorPosition(0);
                    currentInput = new StringBuilder();
                    return;
                }
            }
        }

        view.showLandingScreen();
    }

    /**
     * Processes a character input during gameplay and updates the current input and suggestions.
     *
     * @param c The character typed by the player.
     * @throws IOException If there is an error updating the screen.
     */
    private void handleCharacter(char c) throws IOException {
        if (c == '[') {
            activateTimeBoost();
        } else if (c == ']') {
            activateTimeSabotage();
        } else {
            currentInput.insert(view.getCursorPosition(), c);
            view.incrementCursorPosition();
            model.updateSuggestions(this.currentInput);
        }
    }

    /**
     * Handles backspace input to remove a character and update suggestions accordingly.
     */
    private void handleBackspace() {
        int cursor = view.getCursorPosition();
        if (cursor > 0 && cursor <= currentInput.length()) {
            currentInput.deleteCharAt(cursor - 1);
            view.decrementCursorPosition();
            model.updateSuggestions(this.currentInput);
        }
    }

    /**
     * Handles Enter key input by validating the selected movie guess and proceeding to the next round if valid.
     *
     * @throws IOException If there is an error updating the view.
     */
    private void handleEnter() throws IOException {
        // if no suggestions, do nothing
        if (model.getSuggestions().isEmpty()) {
            return;
        }
        Movie guess;
        selectedTitle = model.getSuggestions().get(model.getSuggestionIndex());
        guess = database.getMovieByName(selectedTitle);
        // if invalid guess, show invalid guess
        if (!model.validateGuess(guess)) {
            view.displayInvalidGuess();
        } else {
            model.updateToNextRound();
            currentInput = new StringBuilder();
            view.setCursorPosition(0);
        }
    }

    /**
     * Displays the game over screen and waits for the user to choose between
     * restarting or quitting the game.
     *
     * @throws IOException if screen updates fail.
     */
    private void gameOver() throws IOException {
        timerRunning = false;
        if (!scheduler.isShutdown()) {
            scheduler.shutdown(); // clean up timer
        }
        boolean waitingForInput = true;
        while (waitingForInput) {
            KeyStroke keyStroke = view.screenPollInput();
            if (keyStroke != null) {
                if (keyStroke.getKeyType() == KeyType.Character) {
                    char choice = Character.toUpperCase(keyStroke.getCharacter());
                    if (choice == 'R') {
                        restartGame();
                        waitingForInput = false;
                    } else if (choice == 'Q') {
                        exitGame();
                        waitingForInput = false;
                    }
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Resets the game state and restarts the view and model for a new session.
     *
     * @throws IOException if resetting view fails.
     */
    private void restartGame() throws IOException {
        model.resetModel(database.getRandomMovie());
        view.resetView();
        currentInput = new StringBuilder();
        timerRunning = true;
    }

    /**
     * Displays the exit screen and marks the game for shutdown.
     *
     * @throws IOException if the view fails to display the exit screen.
     */
    private void exitGame() throws IOException {
        view.showExitScreen();
        shouldExit = true;
    }

    /**
     * Activates the time boost power-up, if available, giving the current player more time.
     *
     * @throws IOException if view fails to display the power-up or update.
     */
    private void activateTimeBoost() throws IOException {
        if ((model.isPlayer1Turn() && model.getPlayer1TimeBoosts() > 0) ||
                (!model.isPlayer1Turn() && model.getPlayer2TimeBoosts() > 0)) {

            model.updateTimeBoosts();
            view.displayPowerUp("Time Boost", true);
            view.updateScreen(this.currentInput);
        } else {
            view.displayPowerUp("Time Boost", false);
            view.updateScreen(this.currentInput);
        }
    }

    /**
     * Activates the time sabotage power-up, if available, reducing the opponent's time.
     *
     * @throws IOException if view fails to display the power-up or update.
     */
    private void activateTimeSabotage() throws IOException {
        boolean isPlayer1Turn = (model.isPlayer1Turn());

        if ((isPlayer1Turn && model.getPlayer1TimeSabotages() > 0) ||
                (!isPlayer1Turn && model.getPlayer2TimeSabotages() > 0)) {
            if (isPlayer1Turn) {
                model.setPlayer1TimeSabotages(model.getPlayer1TimeSabotages() - 1);
            } else {
                model.setPlayer2TimeSabotages(model.getPlayer2TimeSabotages() - 1);
            }
            model.setNextPlayerSabotaged(true);
            view.displayPowerUp("Time Sabotage", true);
            view.updateScreen(this.currentInput);
        } else {
            view.displayPowerUp("Time Sabotage", false);
            view.updateScreen(this.currentInput);
        }
    }

    /**
     * Handles updates to the controller from the model based on game events.
     * If the event indicates a game over, it triggers the end-game flow.
     *
     * @param event The type of event that occurred (e.g., "REFRESH", "GAME_OVER_1").
     */
    @Override
    public void update(String event) {
        System.out.println("Controller received event: " + event);
        if (event.equals("GAME_OVER_1") || event.equals("GAME_OVER_2")) {
            try {
                gameOver();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
