import org.junit.Before;
import org.junit.Test;

import java.util.*;

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

        suggestions = new HashSet<>(Set.of("titanic (1997)", "titans (2000)",
                "the pact (2002)", "500 days of summer (2015)"));

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
        assertTrue(model.isPlayer1Turn());
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

        assertEquals(0, model.getSelectedGenreIndex());
        model.selectNextGenre(3, genreList.length);
        assertEquals(3, model.getSelectedGenreIndex());
    }

    @Test
    public void testIncrementAndDecrementPlayerNames() {
        assertEquals("", model.getPlayer1Name());
        model.setEnteringPlayer1(true);
        model.incrementPlayerNames('a');
        assertEquals("a", model.getPlayer1Name());
        model.decrementPlayerNames();
        assertEquals("", model.getPlayer1Name());

        assertEquals("", model.getPlayer2Name());
        model.setEnteringPlayer1(false);
        model.incrementPlayerNames('b');
        assertEquals("b", model.getPlayer2Name());
        model.decrementPlayerNames();
        assertEquals("", model.getPlayer1Name());
    }

    @Test
    public void testUpdateSuggestions() {
        assertEquals(0, model.getSuggestions().size());
        StringBuilder input = new StringBuilder("T");
        model.updateSuggestions(input);
        assertEquals(3, model.getSuggestions().size());
        assertTrue(suggestions.containsAll(model.getSuggestions()));
    }

    @Test
    public void testMoveSuggestionsAllDirections() {
        assertEquals(0, model.getSuggestions().size());
        StringBuilder input = new StringBuilder("T");
        model.updateSuggestions(input);
        assertEquals(3, model.getSuggestions().size());

        assertEquals(0, model.getSuggestionIndex());
        model.moveSuggestionDown();
        assertEquals(1, model.getSuggestionIndex());
        model.moveSuggestionUp();
        assertEquals(0, model.getSuggestionIndex());
        model.moveSuggestionRight();
        assertEquals(2, model.getSuggestionIndex());
        model.moveSuggestionLeft();
        assertEquals(0, model.getSuggestionIndex());
    }

    @Test
    public void testValidateGuessWhenGuessedBefore() {
        assertFalse(model.validateGuess(titanic));
    }

    @Test
    public void testValidateGuessWhenNoConnections() {
        String title = "Movie2";
        int releaseDate = 2000;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("Some Director"));
        Set<String> actors = new HashSet<>(Set.of("Actor1", "Actor2"));
        Set<String> writers = new HashSet<>(Set.of("Writer1"));
        Set<String> cinematographers = new HashSet<>(Set.of("Cinematographer1", "Cinematographer2"));
        Set<String> composers = new HashSet<>(Set.of("Composer1"));
        Movie movie = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);

        assertFalse(model.validateGuess(movie));
    }

    @Test
    public void testValidateGuessWhenConnectedLimitHit() {
        String title = "Movie2";
        int releaseDate = 2000;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("Some Director"));
        Set<String> actors = new HashSet<>(Set.of("Leo DiCaprio", "Actor2"));
        Set<String> writers = new HashSet<>(Set.of("Writer1"));
        Set<String> cinematographers = new HashSet<>(Set.of("Cinematographer1", "Cinematographer2"));
        Set<String> composers = new HashSet<>(Set.of("Composer1"));
        Movie movie = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);

        Player player1 = model.getCurrentPlayer();
        Map<String, Integer> connections = new HashMap<>();
        connections.put("Leo DiCaprio", 3);
        player1.setConnections(connections);

        assertFalse(model.validateGuess(movie));
    }

    @Test
    public void testValidateGuessForValidGuess() {
        String title = "Movie2";
        int releaseDate = 2000;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("Some Director"));
        Set<String> actors = new HashSet<>(Set.of("Leo DiCaprio", "Actor2"));
        Set<String> writers = new HashSet<>(Set.of("Writer1"));
        Set<String> cinematographers = new HashSet<>(Set.of("Cinematographer1", "Cinematographer2"));
        Set<String> composers = new HashSet<>(Set.of("Composer1"));
        Movie movie = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);
        assertTrue(model.validateGuess(movie));
    }

    @Test
    public void testDecrementSeconds() {
        assertEquals(30, model.getSecondsRemaining());
        for (int i = 0; i < 19; i++) {
            model.decrementSecondsRemaining();
        }
        assertEquals(11, model.getSecondsRemaining());
    }

    @Test
    public void testUpdateToNextRound() {
        assertEquals(0, model.getRoundNumber());
        StringBuilder input = new StringBuilder("T");
        model.updateSuggestions(input);
        assertEquals(3, model.getSuggestions().size());
        assertEquals(30, model.getSecondsRemaining());

        for (int i = 0; i < 15; i++) {
            model.decrementSecondsRemaining();
        }

        assertEquals(15, model.getSecondsRemaining());
        model.updateToNextRound();

        assertEquals(1, model.getRoundNumber());
        assertEquals(0, model.getSuggestions().size());
        assertEquals(30, model.getSecondsRemaining());
    }

    @Test
    public void testUpdateTimeBoosts() {
        assertEquals(30, model.getSecondsRemaining());
        assertEquals(2, model.getPlayer1TimeBoosts());
        model.updateTimeBoosts();
        assertEquals(45, model.getSecondsRemaining());
        assertEquals(1, model.getPlayer1TimeBoosts());
        model.updateTimeBoosts();
        assertEquals(60, model.getSecondsRemaining());
        assertEquals(0, model.getPlayer1TimeBoosts());
    }

    @Test
    public void testStartNewGame() {
        model.setEnteringPlayer1(true);
        model.incrementPlayerNames('a');
        model.incrementPlayerNames('b');
        model.incrementPlayerNames('c');
        assertEquals(0, model.getSelectedGenreIndex());
        assertEquals("", model.getSelectedGenre());
        assertFalse(model.isGameStarted());

        model.startNewGame();

        assertTrue(model.isGameStarted());
        Player player1 = model.getCurrentPlayer();
        assertEquals("abc", player1.getUsername());
        assertEquals("Action", model.getSelectedGenre());
    }

    @Test
    public void testResetModel() {
        // changes to game state
        model.startNewGame();
        model.updateToNextRound();
        model.setPlayer1TimeSabotages(0);
        for (int i = 0 ; i < 15; i++) {
            model.decrementSecondsRemaining();
        }
        model.setEnteringPlayer1(false);
        model.setNextPlayerSabotaged(true);
        model.setPlayer1TimeSabotages(50);
        model.setPlayer2TimeSabotages(55);


        Deque<Movie> lastFiveMovies = new ArrayDeque<>();
        lastFiveMovies.add(titanic);
        assertFalse(model.getEnteringPlayer1());
        assertSame(model.getLastFiveMovies().peekFirst(), titanic);
        assertTrue(model.getLastFiveConnections().containsKey("Titanic"));
        assertNull(model.getLastFiveConnections().get("Titanic"));
        assertTrue(model.getLastFivePlayers().containsKey("Titanic"));
        assertNull(model.getLastFivePlayers().get("Titanic"));
        assertEquals("Action", model.getSelectedGenre());
        assertEquals(0, model.getSelectedGenreIndex());
        assertEquals(1, model.getRoundNumber());
        assertTrue(model.isGameStarted());
        assertTrue(model.isNextPlayerSabotaged());
        assertEquals(50, model.getPlayer1TimeSabotages());
        assertEquals(55, model.getPlayer2TimeSabotages());
        assertEquals(15, model.getSecondsRemaining());

        String title = "Movie2";
        int releaseDate = 2000;
        Set<String> genres = new HashSet<>(Set.of("Drama", "Family", "Adventure"));
        Set<String> directors = new HashSet<>(Set.of("Some Director"));
        Set<String> actors = new HashSet<>(Set.of("Actor1", "Actor2"));
        Set<String> writers = new HashSet<>(Set.of("Writer1"));
        Set<String> cinematographers = new HashSet<>(Set.of("Cinematographer1", "Cinematographer2"));
        Set<String> composers = new HashSet<>(Set.of("Composer1"));
        Movie movie = new Movie(title, releaseDate, genres, directors, actors, writers, cinematographers, composers);
        model.resetModel(movie);

        lastFiveMovies = new ArrayDeque<>();
        lastFiveMovies.add(movie);
        assertTrue(model.getEnteringPlayer1());
        assertSame(model.getLastFiveMovies().peekFirst(), movie);
        assertTrue(model.getLastFiveConnections().containsKey("Movie2"));
        assertNull(model.getLastFiveConnections().get("Movie2"));
        assertTrue(model.getLastFivePlayers().containsKey("Movie2"));
        assertNull(model.getLastFivePlayers().get("Movie2"));
        assertEquals("", model.getSelectedGenre());
        assertFalse(model.isSelectingGenre());
        assertEquals(0, model.getRoundNumber());
        assertFalse(model.isGameStarted());
        assertFalse(model.isNextPlayerSabotaged());
        assertEquals(1, model.getPlayer1TimeSabotages());
        assertEquals(1, model.getPlayer1TimeSabotages());
        assertEquals(30, model.getSecondsRemaining());
    }

}