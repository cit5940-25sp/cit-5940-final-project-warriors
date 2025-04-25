public class MovieGameController{
    private static final int TIME_LIMIT = 15;
    private MovieGameModel model;
    private MovieGameView view;
    private Database database;
    private Thread timerThread;
    private boolean timeUp;
    private int numRounds;
    private boolean gameOver;

    /**
     * Initialize a new MovieGameController
     */
    public MovieGameController(String username1, String username2) {
        this.database = new Database();
        Movie startingMovie = database.getRandomMovie(); // get starting movie from database
        this.model = new MovieGameModel(username1, username2, startingMovie);
        this.view = new MovieGameView(model, database.getMovieNameSet());
        this.numRounds = 0;
        this.gameOver = false;

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
        while (!gameOver) {
            newRound();
        }
    }

    /**
     * Initiate a new round
     */
    public void newRound() {
        timerThread.start();

        // gets user input on main thread
        String name = MovieGameView.getUserGuess();
        Movie guess = database.getMovieByName(name);

        if (!timeUp) {
            // User guessed in time
            timerThread.interrupt(); // stop timer thread
            if (!model.validateGuess(guess)) {
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
