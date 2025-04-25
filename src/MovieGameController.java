import java.util.Timer;

public class MovieGameController{
    private static final int TIME_LIMIT = 15;
    private GameState gameState;
    private MovieGameModel gameModel;
    private Thread timerThread;
    private boolean timeUp;
    private int numRounds;

    /**
     * Initialize a new MovieGameController
     */
    public MovieGameController(String username1, String username2) {
        this.gameModel = new MovieGameModel();
        Movie startingMovie = gameModel.getRandomMovie(); // get starting movie
        this.gameState = new GameState(username1, username2, startingMovie);
        this.numRounds = 0;

        // initialize time thread runnable input
        timerThread = new Thread(() -> {
            int seconds = TIME_LIMIT;
            this.timeUp = false;
            try {
                while (seconds > 0) {
                    Thread.sleep(1000);
                    seconds--;
                }
                System.out.println("Time's up!");
                timeUp = true;
            } catch (InterruptedException e) {
                // Timer was stopped early
                System.out.println("Timer interrupted (user guessed in time).");
            }
        });
    }

    /**
     * Run game
     */
    public void setUp() {
        while (true) {
            newRound();
        }
    }

    /**
     * Initiate a new round
     */
    public void newRound() {
        timerThread.start();

        // gets user input on main thread
        Movie guess = MovieGameView.getUserGuess();

        if (!timeUp) {
            // User guessed in time
            timerThread.interrupt(); // stop timer thread
            if (!gameState.validateGuess(guess)) {
                System.out.println("Invalid guess.");
            } else {
                System.out.println("Good guess!");
            }
        } else {
            // Timer ran out before guess
            System.out.println("Too late!");
        }
    }

}
