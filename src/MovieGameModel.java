import java.util.*;

public class MovieGameModel implements IObservable {

    private GameState gameState;
    private List<IObserver> observers;
    private boolean hasChanged = false;

    /**
     * Initialize a new MovieGameModel
     */
    public MovieGameModel() {
        this.gameState = new GameState();
        this.observers = new ArrayList<>();
    }

    /**
     * @return the current gameState
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Adds a new observer
     */
    @Override
    public void addObserver(IObserver o) {
        observers.add(o);
    }

    /**
     * Removes an observer
     */
    @Override
    public void removeObserver(IObserver o) {
        observers.remove(o);
    }

    /**
     * Clears all observers
     */
    @Override
    public void removeAllObservers() {
        observers.clear();
    }

    /**
     * Update observer status
     */
    @Override
    public void setChanged() {
        hasChanged = true;
    }

    /**
     * Reset an observer
     */
    @Override
    public void clearChanged() {
        hasChanged = false;
    }

    /**
     * @return if an observer has been updated
     */
    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

    /**
     * Updates observers if there has been a change
     */
    @Override
    public void notifyObservers(String event) {
        if (hasChanged) {
            List<IObserver> copy = new ArrayList<>(observers);
            clearChanged();

            for (IObserver observer : copy) {
                observer.update(event);
            }
        }
    }

}
