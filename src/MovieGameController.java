import java.util.ArrayDeque;
import java.util.Queue;

public class MovieGameController{
    GameState gameState;
    MovieGameModel gameModel;
    GameRules gameRules;
    Timer timer;

    /**
     * Initialize a new MovieGameController
     */
    public MovieGameController(String username1, String username2) {
        this.gameState = new GameState(username1, username2);
        this.gameModel = new MovieGameModel();
        this.gameRules = new GameRules();
        this.timer = new Timer();
    }

    public void setupGame() {
        // are we doing this terminal style?
    }












}
