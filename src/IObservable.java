public interface IObservable {

    // adds an observer
    public void addObserver(IObserver observer);

    // removes an observer
    public void removeObserver(IObserver observer);

    // notify observers that there is an update
    public void notifyObservers();
}
