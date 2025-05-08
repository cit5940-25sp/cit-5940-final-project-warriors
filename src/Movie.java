import java.util.*;

/**
 * Represents a movie with a title, release date, genres, and directors, actors,
 * writers, cinematographers, and composers.
 */
public class Movie {
    private String title;
    private int releaseDate;
    private Set<String> genres;
    private Set<String> directors;
    private Set<String> actors;
    private Set<String> writers;
    private Set<String> cinematographers;
    private Set<String> composers;

    /**
     * Constructs a new Movie object with the specified details.
     *
     * @param title            the title of the movie
     * @param releaseDate      the release year of the movie
     * @param genres           the genres the movie belongs to
     * @param directors        the directors of the movie
     * @param actors           the actors in the movie
     * @param writers          the writers of the movie
     * @param cinematographers the cinematographers of the movie
     * @param composers        the composers of the movie's score
     */
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
    }

    /**
     * @return the movie's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the release date of the movie
     */
    public int getReleaseDate() {
        return releaseDate;
    }

    /**
     * @return a set of genres associated with the movie
     */
    public Set<String> getGenres() {
        return genres;
    }

    /**
     * @return the directors of the movie
     */
    public Set<String> getDirectors() {
        return directors;
    }

    /**
     * @return the actors of the movie
     */
    public Set<String> getActors() {
        return actors;
    }

    /**
     * @return the writers of the movie
     */
    public Set<String> getWriters() {
        return writers;
    }

    /**
     * @return the composers of the movie
     */
    public Set<String> getComposers() {
        return composers;
    }

    /**
     * Returns a set containing all people of the movie.
     *
     * @return a set of all contributor names
     */
    public Set<String> getAllPeople() {
        Set<String> result = new HashSet<>();
        result.addAll(directors);
        result.addAll(actors);
        result.addAll(writers);
        result.addAll(cinematographers);
        result.addAll(composers);
        return result;
    }

    /**
     * Returns a string representation of the movie, including the title, release date, and genres.
     *
     * @return a string describing the movie
     */
    @Override
    public String toString() {
        return title + ", " + releaseDate + ", " + genres;
    }

    /**
     * Checks if this movie is equal to another object based on the title.
     *
     * @param obj the object to compare with
     * @return true if the titles are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Movie other = (Movie) obj;
        return Objects.equals(this.getTitle(), other.getTitle());
    }

    /**
     * Returns a hash code value for the movie based on its title.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getTitle());
    }


}
