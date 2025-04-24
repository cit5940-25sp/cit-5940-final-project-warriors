import java.util.*;

public class Movie {
    private String title;
    private int releaseDate;
    private Set<String> genres;
    private Set<String> directors;
    private Set<String> actors;
    private Set<String> writers;
    private Set<String> cinematographers;
    private Set<String> composers;
    private Set<String> allPeople;

    public Movie(String title, int releaseDate,
                 Set<String> genres,
                 Set<String> directors,
                 Set<String> actors,
                 Set<String> writers,
                 Set<String> cinematographers,
                 Set<String> composers) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.directors = directors;
        this.actors = actors;
        this.writers = writers;
        this.cinematographers = cinematographers;
        this.composers = composers;
        this.allPeople = new HashSet<>();
        allPeople.addAll(directors);
        allPeople.addAll(actors);
        allPeople.addAll(writers);
        allPeople.addAll(cinematographers);
        allPeople.addAll(composers);
    }

    public String getTitle() {
        return title;
    }

    public void SetTitle(String title) {
        this.title = title;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public void SetReleaseDate(int releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public void SetGenres(Set<String> genres) {
        this.genres = genres;
    }

    public Set<String> getDirectors() {
        return directors;
    }

    public void SetDirectors(Set<String> directors) {
        this.directors = directors;
    }

    public Set<String> getActors() {
        return actors;
    }

    public void SetActors(Set<String> actors) {
        this.actors = actors;
    }

    public Set<String> getWriters() {
        return writers;
    }

    public void SetWriters(Set<String> writers) {
        this.writers = writers;
    }

    public Set<String> getCinematographers() {
        return cinematographers;
    }

    public void SetCinematographers(Set<String> cinematographers) {
        this.cinematographers = cinematographers;
    }

    public Set<String> getComposers() {
        return composers;
    }

    public void SetComposers(Set<String> composers) {
        this.composers = composers;
    }

    public Set<String> getAllPeople() {
        return allPeople;
    }

    @Override
    public String toString() {
        return title + ", " + releaseDate + ", " + genres;
    }

}
