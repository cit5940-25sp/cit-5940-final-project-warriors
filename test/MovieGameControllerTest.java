import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

@RunWith(MockitoJUnitRunner.class)
public class MovieGameControllerTest {

    @Mock
    private MovieGameModel modelMock;

    @Mock
    private MovieGameView viewMock;

    @Mock
    private Database databaseMock;

    @Mock
    private ScheduledExecutorService schedulerMock;

    @InjectMocks
    private MovieGameController controller;

    private final Movie testMovie = new Movie(
            "Titanic",
            2010,
            Set.of("Family", "Animation"),
            Set.of("Director1", "Director2"),
            Set.of("Actor1", "Actor2"),
            Set.of("Writer1"),
            Set.of("Producer1"),
            Set.of("Composer1")
    );

    @Test
    public void testHandleCharacter() throws IOException {
        when(viewMock.getCursorPosition()).thenReturn(0);
        controller.currentInput = new StringBuilder();
        controller.handleCharacter('a');

        assertEquals("a", controller.currentInput.toString());
        verify(modelMock).updateSuggestions(controller.currentInput);
        verify(viewMock).incrementCursorPosition();
    }

    @Test
    public void testHandleBackspace() throws IOException {
        controller.currentInput = new StringBuilder("test");
        when(viewMock.getCursorPosition()).thenReturn(4);
        controller.handleBackspace();

        assertEquals("tes", controller.currentInput.toString());
        verify(modelMock).updateSuggestions(controller.currentInput);
        verify(viewMock).decrementCursorPosition();
    }

    @Test
    public void testHandleBackspace2() throws IOException {
        controller.currentInput = new StringBuilder("test");
        when(viewMock.getCursorPosition()).thenReturn(0);
        controller.handleBackspace();

        assertEquals("test", controller.currentInput.toString());
        verifyNoInteractions(modelMock);
        verify(viewMock, never()).decrementCursorPosition();
        verify(viewMock, never()).updateScreen(any());
    }

    @Test
    public void testHandleEnter() throws IOException {
        controller.currentInput = new StringBuilder("Tangled");
        when(modelMock.getSuggestions()).thenReturn(Collections.singletonList("Tangled"));
        when(modelMock.getSuggestionIndex()).thenReturn(0);
        when(databaseMock.getMovieByName("Tangled")).thenReturn(testMovie);
        when(modelMock.validateGuess(testMovie)).thenReturn(true);
        controller.handleEnter();

        verify(modelMock).updateToNextRound();
        assertEquals(0, controller.currentInput.length());
        verify(viewMock).setCursorPosition(0);
    }

    @Test
    public void testHandleEnter2() throws IOException {
        controller.currentInput = new StringBuilder("Invalid");
        when(modelMock.getSuggestions()).thenReturn(Collections.singletonList("Invalid"));
        when(modelMock.getSuggestionIndex()).thenReturn(0);
        when(databaseMock.getMovieByName("Invalid")).thenReturn(null);
        controller.handleEnter();

        verify(viewMock).displayInvalidGuess();
        verify(modelMock, never()).updateToNextRound();
    }


    @Test
    public void testCleanup() throws IOException {
        controller.cleanup();
        verify(schedulerMock).shutdown();
        verify(viewMock).cleanUp();
    }

    @Test
    public void testActivateTimeBoost() throws IOException {
        when(modelMock.isPlayer1Turn()).thenReturn(true);
        when(modelMock.getPlayer1TimeBoosts()).thenReturn(1);
        controller.activateTimeBoost();

        verify(modelMock).updateTimeBoosts();
        verify(viewMock).displayPowerUp("Time Boost", true);
    }

    @Test
    public void testActivateTimeBoost2() throws IOException {
        when(modelMock.isPlayer1Turn()).thenReturn(true);
        when(modelMock.getPlayer1TimeBoosts()).thenReturn(0);
        controller.activateTimeBoost();

        verify(modelMock, never()).updateTimeBoosts();
        verify(viewMock).displayPowerUp("Time Boost", false);
    }

    @Test
    public void testHandleLandingInput() throws IOException {
        when(modelMock.isSelectingGenre()).thenReturn(false);
        KeyStroke keyStroke = new KeyStroke('a', false, false);
        controller.handleLandingInput(keyStroke);

        verify(modelMock).incrementPlayerNames('a');
        verify(viewMock).showLandingScreen();
    }

    @Test
    public void testHandleLandingInput2() throws IOException {
        when(modelMock.isSelectingGenre()).thenReturn(false);
        when(modelMock.getEnteringPlayer1()).thenReturn(true);
        when(modelMock.getPlayer1Name()).thenReturn("Player1");
        KeyStroke keyStroke = new KeyStroke(KeyType.Enter);

        controller.handleLandingInput(keyStroke);
        verify(modelMock).setEnteringPlayer1(false);
        verify(viewMock).showLandingScreen();
    }

    @Test
    public void testUpdate() throws IOException {
        String event = "TEST";
        controller.update(event);

        verifyNoInteractions(viewMock);
        verify(schedulerMock, never()).shutdown();
    }

    @Test
    public void testActivateTimeSabotage() throws IOException {
        when(modelMock.isPlayer1Turn()).thenReturn(true);
        when(modelMock.getPlayer1TimeSabotages()).thenReturn(1);
        controller.activateTimeSabotage();

        verify(modelMock).setPlayer1TimeSabotages(0);
        verify(modelMock).setNextPlayerSabotaged(true);
        verify(viewMock).displayPowerUp("Time Sabotage", true);
    }

    @Test
    public void testActivateTimeSabotage2() throws IOException {
        when(modelMock.isPlayer1Turn()).thenReturn(true);
        when(modelMock.getPlayer1TimeSabotages()).thenReturn(0);
        controller.activateTimeSabotage();

        verify(modelMock, never()).setPlayer1TimeSabotages(anyInt());
        verify(modelMock, never()).setNextPlayerSabotaged(anyBoolean());
        verify(viewMock).displayPowerUp("Time Sabotage", false);
    }

    @Test
    public void testRestartGame() throws IOException {
        controller.currentInput = new StringBuilder("test");
        controller.timerRunning = false;
        when(databaseMock.getRandomMovie()).thenReturn(testMovie);
        controller.restartGame();

        assertEquals(0, controller.currentInput.length());
        assertTrue(controller.timerRunning);
        verify(modelMock).resetModel(testMovie);
        verify(viewMock).resetView();
    }

    @Test
    public void testGameOver() throws IOException {
        controller.timerRunning = true;
        when(viewMock.screenPollInput())
                .thenReturn(new KeyStroke('r', false, false))  // First try (lowercase)
                .thenReturn(new KeyStroke('R', false, false)); // Second try (uppercase)
        controller.gameOver();

        verify(schedulerMock).shutdown();
    }

    @Test
    public void testGameOver2() throws IOException {
        when(viewMock.screenPollInput())
                .thenReturn(new KeyStroke('R', false, false));
        when(databaseMock.getRandomMovie()).thenReturn(testMovie);
        controller.gameOver();

        verify(modelMock).resetModel(testMovie);
        verify(viewMock).resetView();
    }

    @Test
    public void testGameOver3() throws IOException {
        when(viewMock.screenPollInput())
                .thenReturn(new KeyStroke('Q', false, false));
        controller.gameOver();

        assertTrue(controller.shouldExit);
        verify(viewMock).showExitScreen();
    }
}