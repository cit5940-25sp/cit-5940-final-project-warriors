import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MovieGameController{
    private MovieGameModel model;
    private MovieGameView view;
    private Database database = new Database();

    private StringBuilder currentInput = new StringBuilder();
    private boolean timerRunning = true;
    private ScheduledExecutorService scheduler;
    private String selectedTitle = "";
    private boolean shouldExit = false;

    /**
     * Initialize a new MovieGameController
     */
    public MovieGameController() throws IOException {
        // initialize database, model, and view
        this.database.loadFromCSV("cleaned_imdb_final.csv");
        this.model = new MovieGameModel(database.getRandomMovie(), database.getMovieNameSet());
        this.view = new MovieGameView(this.model);

        // schedule timer
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (timerRunning && model.getSecondsRemaining() > 0 && model.isGameStarted()) {
                model.decrementSecondsRemaining();
                try {
                    view.updateScreen(this.currentInput);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (model.getSecondsRemaining() == 0 && model.isGameStarted()) {
                try {
                    gameOver();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void run() throws IOException {
        boolean running = true;
        view.resetView();

        while (running && !shouldExit) {
            KeyStroke keyStroke = view.screenPollInput();
            if (keyStroke != null) {
                // game not started - show landing page
                if (!model.isGameStarted()) {
                    handleLandingInput(keyStroke);
                    continue;
                }

                if (model.getSecondsRemaining() == 0) {
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

    private void cleanup() throws IOException {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        view.cleanUp();
    }

    private void handleLandingInput(KeyStroke keyStroke) throws IOException {
        if (keyStroke.getKeyType() == KeyType.Character) {
            model.incrementPlayerNames(keyStroke.getCharacter());
        } else if (keyStroke.getKeyType() == KeyType.Backspace) {
            model.decrementPlayerNames();
        } else if (keyStroke.getKeyType() == KeyType.Enter) {
            if (model.getEnteringPlayer1() && !model.getPlayer1Name().isEmpty()) {
                model.setEnteringPlayer1(false);
            } else if (!model.getEnteringPlayer1() && !model.getPlayer2Name().isEmpty()) {
                model.setGameStarted(true);
                model.initializePlayerNames();
                currentInput = new StringBuilder();
                view.setCursorPosition(0);
                model.notifyObservers("GAME_START");
            }
        }
        view.showLandingScreen();
    }

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

    private void handleBackspace() {
        if (view.getCursorPosition() > 0) {
            currentInput.deleteCharAt(view.getCursorPosition() - 1);
            view.decrementCursorPosition();
            model.updateSuggestions(this.currentInput);
        }
    }

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

    public void gameOver() throws IOException {
        view.showGameOverScreen();
        model.notifyObservers("GAME_OVER");

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

    private void restartGame() throws IOException {
        model.resetModel(database.getRandomMovie());
        view.resetView();
        currentInput = new StringBuilder();
    }

    private void exitGame() throws IOException {
        view.showExitScreen();
        shouldExit = true;
    }

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

}
