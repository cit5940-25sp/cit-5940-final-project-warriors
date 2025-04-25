import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class MovieGameView implements IObserver {
    private MovieGameModel model;
    private Set<String> movieNames;
    private Autocomplete autocomplete;

    /**
     * Initializes a new MovieGameView
     */
    public MovieGameView(MovieGameModel model, Set<String> movieNames) {
        this.model = model;
        this.movieNames = movieNames;
        this.autocomplete = new Autocomplete();
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

    /**
     * Gets the user's guess for a round of gameplay. Implements AutoComplete.
     * @return String name of guess
     */
    public static String getUserGuess() {
        return "";
    }

}
