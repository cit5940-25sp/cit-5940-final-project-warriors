import java.util.*;

public class MovieGameModel implements IObservable {

    private GameState gameState;
    private List<IObserver> observers;

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
    public void notifyObservers() {

    }

}
