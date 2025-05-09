import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class MovieTest {

    @Test
    public void testGetPeople() {
        String title = "Titanic";
        int releaseDate = 1997;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("James Cameron"));
        Set<String> actors = new HashSet<>(Set.of("Leo DiCaprio", "Kate Winslet"));
        Set<String> writers = new HashSet<>(Set.of("James Cameron"));
        Set<String> cinematographers = new HashSet<>(Set.of("Russell Carpenter", "John M. Stephens"));
        Set<String> composers = new HashSet<>(Set.of("Hans Zimmer"));
        Movie movie = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);

        assertEquals("Titanic", movie.getTitle());
        assertEquals(1997, movie.getReleaseDate());
        assertEquals(genres, movie.getGenres());
        assertEquals(directors, movie.getDirectors());
        assertEquals(actors, movie.getActors());
        assertEquals(writers, movie.getWriters());
        assertEquals(cinematographers, movie.getCinematographers());
        assertEquals(composers, movie.getComposers());
    }

    @Test
    public void testGetAllPeople() {
        String title = "Titanic";
        int releaseDate = 1997;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("James Cameron"));
        Set<String> actors = new HashSet<>(Set.of("Leo DiCaprio", "Kate Winslet"));
        Set<String> writers = new HashSet<>(Set.of("James Cameron"));
        Set<String> cinematographers = new HashSet<>(Set.of("Russell Carpenter", "John M. Stephens"));
        Set<String> composers = new HashSet<>(Set.of("Hans Zimmer"));
        Movie movie = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);

        Set<String> allPeople = new HashSet<>();
        allPeople.add("James Cameron");
        allPeople.add("Leo DiCaprio");
        allPeople.add("Kate Winslet");
        allPeople.add("Russell Carpenter");
        allPeople.add("John M. Stephens");
        allPeople.add("Hans Zimmer");
        assertEquals(6, movie.getAllPeople().size());
        assertTrue(allPeople.containsAll(movie.getAllPeople()));
    }

    @Test
    public void testToString() {
        String title = "Titanic";
        int releaseDate = 1997;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("James Cameron"));
        Set<String> actors = new HashSet<>(Set.of("Leo DiCaprio", "Kate Winslet"));
        Set<String> writers = new HashSet<>(Set.of("James Cameron"));
        Set<String> cinematographers = new HashSet<>(Set.of("Russell Carpenter", "John M. Stephens"));
        Set<String> composers = new HashSet<>(Set.of("Hans Zimmer"));
        Movie movie = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);

        assertEquals("Titanic, 1997, [Adventure, Drama, Family]", movie.toString());
    }
}