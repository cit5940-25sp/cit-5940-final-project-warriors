import java.util.Observable;

public interface IObserver {

    /**
     * Update the GameView based on the event that
     * has occurred (i.e. GAME_START, VALID_MOVE,
     * INVALID_MOVE, GAME_END)
     */
    public void update(String event);

}
