import java.util.*;

public class MovieGameModel implements IObservable {

    private GameState gameState;
    private List<IObserver> observers;
    private boolean hasChanged = false;

    public MovieGameModel() {
        this.gameState = new GameState();
        this.observers = new ArrayList<>();
    }

    public GameState getGameState() {
        return gameState;
    }


    @Override
    public void addObserver(IObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(IObserver o) {
        observers.remove(o);
    }

    @Override
    public void removeAllObservers() {
        observers.clear();
    }

    @Override
    public void setChanged() {
        hasChanged = true;
    }

    @Override
    public void clearChanged() {
        hasChanged = false;
    }

    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

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
