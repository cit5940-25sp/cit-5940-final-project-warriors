import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class MovieGameModelTest {
    Movie titanic;
    MovieGameModel model;
    Set<String> suggestions;
    String[] genreList = {
            "Action", "Adventure", "Animation", "Comedy",
            "Crime", "Drama", "Family", "Fantasy",
            "Horror", "Romance", "Sci-Fi", "Thriller"
    };

    @Before
    public void setUp() throws Exception {
        String title = "Titanic";
        int releaseDate = 1997;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("James Cameron"));
        Set<String> actors = new HashSet<>(Set.of("Leo DiCaprio", "Kate Winslet"));
        Set<String> writers = new HashSet<>(Set.of("James Cameron"));
        Set<String> cinematographers = new HashSet<>(Set.of("Russell Carpenter", "John M. Stephens"));
        Set<String> composers = new HashSet<>(Set.of("Hans Zimmer"));
        titanic = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);

        suggestions = new HashSet<>(Set.of("Titanic (1997)", "Titans (2000)", "The Pact (2002)"));

        model = new MovieGameModel(titanic, suggestions);
    }

    @Test
    public void testGettersUponInitialization() {
        Deque<Movie> lastFiveMovies = new ArrayDeque<>();
        lastFiveMovies.add(titanic);

        assertEquals("", model.getPlayer1Name());
        assertEquals("", model.getPlayer2Name());
        assertTrue(model.getEnteringPlayer1());
        assertSame(model.getLastFiveMovies().peekFirst(), titanic);
        assertTrue(model.getLastFiveConnections().containsKey("Titanic"));
        assertNull(model.getLastFiveConnections().get("Titanic"));
        assertTrue(model.getLastFivePlayers().containsKey("Titanic"));
        assertNull(model.getLastFivePlayers().get("Titanic"));
        assertEquals("", model.getSelectedGenre());
        assertEquals(0, model.getSelectedGenreIndex());
        assertFalse(model.isSelectingGenre());
        assertEquals(genreList, model.getGenreList());
        assertEquals(0, model.getRoundNumber());
        assertFalse(model.isGameStarted());
        assertEquals(0, model.getPlayer1Score());
        assertEquals(0, model.getPlayer1Score());
        assertFalse(model.isNextPlayerSabotaged());
        assertEquals(2, model.getPlayer1TimeBoosts());
        assertEquals(2, model.getPlayer2TimeBoosts());
        assertEquals(1, model.getPlayer1TimeSabotages());
        assertEquals(1, model.getPlayer1TimeSabotages());
        assertEquals(0, model.getSuggestions().size());
        assertEquals(30, model.getSecondsRemaining());
    }

    @Test
    public void testSetters() {
        assertTrue(model.getEnteringPlayer1());
        model.setEnteringPlayer1(false);
        assertFalse(model.getEnteringPlayer1());

        assertFalse(model.isSelectingGenre());
        model.setSelectingGenre(true);
        assertTrue(model.isSelectingGenre());

        assertFalse(model.isGameStarted());
        model.setGameStarted(true);
        assertTrue(model.isGameStarted());

        assertEquals(1, model.getPlayer1TimeSabotages());
        model.setPlayer1TimeSabotages(100);
        assertEquals(100, model.getPlayer1TimeSabotages());

        assertEquals(1, model.getPlayer2TimeSabotages());
        model.setPlayer2TimeSabotages(50);
        assertEquals(50, model.getPlayer2TimeSabotages());

        assertFalse(model.isNextPlayerSabotaged());
        model.setNextPlayerSabotaged(true);
        assertTrue(model.isNextPlayerSabotaged());

        assertEquals(0, model.getSuggestionIndex());
        model.setSuggestionIndex(29);
        assertEquals(29, model.getSuggestionIndex());
    }

}