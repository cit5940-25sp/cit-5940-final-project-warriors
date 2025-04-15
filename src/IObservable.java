public interface IObservable {

    /**
     * Adds a new observer
     */
    public void addObserver(IObserver observer);

    /**
     * Removes an observer
     */
    public void removeObserver(IObserver observer);

    /**
     * Updates observers if there has been a change
     */
    public void notifyObservers(String event);

    /**
     * Clears all observers
     */
    public void removeAllObservers();

    /**
     * Update observer status
     */
    public void setChanged();

    /**
     * Reset an observer
     */
    public void clearChanged();

    /**
     * @return if an observer has been updated
     */
    public boolean hasChanged();
}
