import java.util.*;

public class Movie {
    private String title;
    private int releaseDate;
    private List<String> genres;
    private List<String> directors;
    private List<String> actors;
    private List<String> writers;
    private List<String> cinematographers;
    private List<String> composers;

    public Movie(String title, int releaseDate,
                 List<String> genres,
                 List<String> directors,
                 List<String> actors,
                 List<String> writers,
                 List<String> cinematographers,
                 List<String> composers) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.directors = directors;
        this.actors = actors;
        this.writers = writers;
        this.cinematographers = cinematographers;
        this.composers = composers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(int releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public void setDirectors(List<String> directors) {
        this.directors = directors;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public List<String> getWriters() {
        return writers;
    }

    public void setWriters(List<String> writers) {
        this.writers = writers;
    }

    public List<String> getCinematographers() {
        return cinematographers;
    }

    public void setCinematographers(List<String> cinematographers) {
        this.cinematographers = cinematographers;
    }

    public List<String> getComposers() {
        return composers;
    }

    public void setComposers(List<String> composers) {
        this.composers = composers;
    }

}
