import java.util.Observable;

public class MovieGameView implements IObserver {
    private MovieGameModel model;

    /**
     * Initializes a new MovieGameView
     */
    public MovieGameView(MovieGameModel model) {
        this.model = model;
        model.addObserver(this);
    }

    /**
     * Update the GameView based on the event that
     * has occurred
     */
    @Override
    public void update(String event) {
        switch (event) {
            case "GAME_START":
                // get new display of game
                break;
            case "VALID_MOVE":
                // update to reflect move made
                break;
            case "INVALID_MOVE":
                // show error message
                break;
            case "GAME_END":
                // show display of end screen
                break;
            default:
                // update the entire view
                break;
        }
    }

}
