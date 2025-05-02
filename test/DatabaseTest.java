import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    @Test
    public void testMoviePeopleMap() {
        Database database = new Database();
        database.loadFromCSV("cleaned_imdb_final.csv");

        Movie Tangled = database.getMovieByName("Tangled (2010)");
        Set<String> actor = Tangled.getActors();
        assertTrue(actor.contains("Brad Garrett"));
        assertTrue(actor.contains("Byron Howard"));
        assertTrue(actor.contains("Delaney Rose Stein"));
        assertTrue(actor.contains("Zachary Levi"));

        Set<String> composer = Tangled.getComposers();
        assertTrue(composer.contains("Alan Menken"));
        assertTrue(composer.contains("Kevin Kliesch"));

        Set<String> director = Tangled.getDirectors();
        assertTrue(director.contains("Byron Howard"));
        assertTrue(director.contains("Dan Cooper"));
        assertTrue(director.contains("Nathan Greno"));

        Set<String> genres = Tangled.getGenres();
        assertTrue(genres.contains("Family"));
        assertTrue(genres.contains("Animation"));

        assertEquals(2010, Tangled.getReleaseDate());
    }
}