import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Observers are classes that need to be notified of changes in an observable object.
 */
public interface IObserver {

    /**
     * Update the GameView based on the event that
     * has occurred (i.e. GAME_START, VALID_MOVE,
     * INVALID_MOVE, GAME_END)
     *
     * @param event that is updated
     */
    public void update(String event);


}
