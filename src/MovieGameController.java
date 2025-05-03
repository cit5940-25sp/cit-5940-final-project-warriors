import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MovieGameController{
    private static final int TIME_LIMIT = 15;

    private MovieGameModel model;
    private MovieGameView view;
    private Database database = new Database();

    private Thread timerThread;
    private boolean timeUp;

    private int numRounds;
    private boolean gameOver;

    private StringBuilder currentInput = new StringBuilder();


    private boolean timerRunning = true;
    private ScheduledExecutorService scheduler;
    private String selectedTitle = "";


    private static final int MAX_HISTORY_SIZE = 5;


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

        // initialize time thread runnable input
//        timerThread = new Thread(() -> {
//            int seconds = TIME_LIMIT;
//            this.timeUp = false;
//            try {
//                while (seconds > 0) {
//                    Thread.sleep(1000);
//                    seconds--;
//                }
//                System.out.println("Time's up!");
//                timeUp = true;
//            } catch (InterruptedException e) {
//                // Timer was stopped early
//                System.out.println("Timer interrupted (user guessed in time).");
//            }
//        });

//        this.model = new MovieGameModel(username1, username2, startingMovie);
//        this.view = new MovieGameView(model, database.getMovieNameSet());
//        this.numRounds = 0;
//        this.gameOver = false;
    }

    public void run() throws IOException {
        boolean running = true;
        view.resetView();

        while (running && !shouldExit) {
            KeyStroke keyStroke = view.screenPollInput();
            if (keyStroke != null) {
                if (!model.isGameStarted()) {
                    handleLandingInput(keyStroke);
                    continue;
                }

                if (model.getSecondsRemaining() == 0) {
                    continue;
                }

                switch (keyStroke.getKeyType()) {
                    case ArrowUp:
                        if (!model.getSuggestions().isEmpty()) {
                            model.setSuggestionIndex(
                                    (model.getSuggestionIndex() - 1 + model.getSuggestions().size()) %
                                            model.getSuggestions().size());
                        }
                        break;
                    case ArrowDown:
                        if (!model.getSuggestions().isEmpty()) {
                            model.setSuggestionIndex(
                                    (model.getSuggestionIndex() + 1) % model.getSuggestions().size());
                        }
                        break;
                    case ArrowLeft:
                        if (!model.getSuggestions().isEmpty() && model.getSuggestionIndex() >= 2) {
                            // Move from right column to left column
                            model.setSuggestionIndex(model.getSuggestionIndex() % 2);
                        }
                        break;
                    case ArrowRight:
                        if (!model.getSuggestions().isEmpty() && model.getSuggestionIndex() < 2) {
                            // Move from left column to right column
                            model.setSuggestionIndex(
                                    Math.min(model.getSuggestionIndex() + 2, model.getSuggestions().size() - 1));
                        }
                        break;
                    case Character:
                        char c = keyStroke.getCharacter();
                        if (c == '1') {
                            activateTimeBoost();
                        } else if (c == '2') {
                            activateTimeSabotage();
                        } else {
                            handleCharacter(Character.toLowerCase(c));
                        }
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
                view.updateScreen(this.currentInput);
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
            if (model.getEnteringPlayer1()) {
                model.updatePlayer1Name(keyStroke.getCharacter());
            } else {
                model.updatePlayer2Name(keyStroke.getCharacter());
            }
        } else if (keyStroke.getKeyType() == KeyType.Backspace) {
            if (model.getEnteringPlayer1() && !model.getPlayer1Name().isEmpty()) {
                String name = model.getPlayer1Name();
                model.setPlayer1Name(name.substring(0, name.length() - 1));
            } else if (!model.getEnteringPlayer1() && !model.getPlayer2Name().isEmpty()) {
                String name = model.getPlayer2Name();
                model.setPlayer2Name(name.substring(0, name.length() - 1));
            }
        } else if (keyStroke.getKeyType() == KeyType.Enter) {
            if (model.getEnteringPlayer1() && !model.getPlayer1Name().isEmpty()) {
                model.setEnteringPlayer1(false);
            } else if (!model.getEnteringPlayer1() && !model.getPlayer2Name().isEmpty()) {
                model.setGameStarted(true);
                currentInput = new StringBuilder();
                view.setCursorPosition(0);
                view.updateScreen(this.currentInput);
            }
        }
        view.showLandingScreen();
    }

    private void handleCharacter(char c) {
        currentInput.insert(view.getCursorPosition(), c);
        view.incrementCursorPosition();
        model.updateSuggestions(this.currentInput);
    }

    private void handleBackspace() {
        if (view.getCursorPosition() > 0) {
            currentInput.deleteCharAt(view.getCursorPosition() - 1);
            view.decrementCursorPosition();
            model.updateSuggestions(this.currentInput);
        }
    }

    private void handleEnter() throws IOException {
        if (!model.getSuggestions().isEmpty()) {
            selectedTitle = model.getSuggestions().get(model.getSuggestionIndex());
            String genre = model.getSuggestionGenres().get(model.getSuggestionIndex());
            model.updateMovieGenres(genre);
        } else {
            selectedTitle = currentInput.toString();
            String genre = "genre";
            model.updateMovieGenres(genre);
        }

        if (!selectedTitle.isEmpty()) {
            selectedTitle = view.capitalizeTitle(selectedTitle);
            model.updateMovieHistory(selectedTitle);

            if (model.getMovieHistory().size() > 1) {
                model.addToConnections(0, generateConnectionPhrase());
                boolean isPlayer1 = (model.getRoundNumber() % 2 == 0);
                model.addToConnectionOwners(0, isPlayer1);

                if (isPlayer1) {
                    model.incrementPlayer1Score();
                } else {
                    model.incrementPlayer2Score();
                }
            }
            model.rotate();
        }

        model.checkTimeSabotage();

        model.setSecondsRemaining(30);
        model.incrementRoundNumber();
        currentInput = new StringBuilder();
        view.setCursorPosition(0);
        model.clearSuggestions();
        model.clearSuggestionGenres();
    }


    // need to change to work for our actual connections
    private String generateConnectionPhrase() {
        return "Connection";
    }

    public void gameOver() throws IOException {
        view.showGameOverScreen();

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
        model.resetModel();
        view.resetView();
        currentInput = new StringBuilder();
    }

    private void exitGame() throws IOException {
        view.showExitScreen();
        shouldExit = true;
    }


    private void activateTimeBoost() throws IOException {
        boolean isPlayer1Turn = (model.getRoundNumber() % 2 == 0);

        if ((isPlayer1Turn && model.getPlayer1TimeBoosts() > 0) || (!isPlayer1Turn && model.getPlayer2TimeBoosts() > 0)) {
            model.updateSecondsRemaining(15);

            if (isPlayer1Turn) {
                model.decrementPlayer1TimeBoosts();
            } else {
                model.decrementPlayer2TimeBoosts();
            }

            view.displayPowerUp("Time Boost", true);
            view.updateScreen(this.currentInput);
        } else {
            view.displayPowerUp("Time Boost", false);
            view.updateScreen(this.currentInput);
        }
    }

    private void activateTimeSabotage() throws IOException {
        boolean isPlayer1Turn = (model.getRoundNumber() % 2 == 0);

        if ((isPlayer1Turn && model.getPlayer1TimeSabotages() > 0) || (!isPlayer1Turn && model.getPlayer2TimeSabotages() > 0)) {
            if (isPlayer1Turn) {
//                player1TimeSabotages--;
                model.setPlayer1TimeSabotages(-1);
            } else {
//                player2TimeSabotages--;
                model.setPlayer2TimeSabotages(-1);
            }
            view.displayPowerUp("Time Sabotage", true);
            view.updateScreen(this.currentInput);
        } else {
            view.displayPowerUp("Time Sabotage", false);
            view.updateScreen(this.currentInput);
        }
    }
//
//    /**
//     * Initiate a new round
//     */
//    public void newRound() {
//        timerThread.start();
//
//        // gets user input on main thread
//        String name = MovieGameView.getUserGuess();
//        Movie guess = database.getMovieByName(name);
//
//        if (!timeUp) {
//            // User guessed in time
//            timerThread.interrupt(); // stop timer thread
//            if (!model.validateGuess(guess)) {
//                System.out.println("Invalid guess.");
//            } else {
//                System.out.println("Good guess!");
//            }
//        } else {
//            // Timer ran out before guess
//            System.out.println("Too late!");
//        }

//    }

}
