import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PlayerTest {
    Player player1 = new Player();
    Player player2 = new Player("player2");


    @Test
    public void getUsername() {
        assertEquals("player2", player2.getUsername());
    }

    @Test
    public void setUsername() {
        assertEquals("player2", player2.getUsername());
        player2.setUsername("abc");
        assertEquals("abc", player2.getUsername());
    }

    @Test
    public void setConnections() {
        assertEquals(0, player1.getConnections().size());
        Map<String, Integer> connections = new HashMap<>();
        connections.put("Leo DiCaprio", 3);
        player1.setConnections(connections);
        assertEquals(0, player1.getConnections().size());

    }

    @Test
    public void getConnectionOfPerson() {

    }

    @Test
    public void updateConnections() {
    }

    @Test
    public void updateCorrectGuesses() {
    }

    @Test
    public void updateIncorrectGuesses() {
    }

    @Test
    public void getScore() {
    }

    @Test
    public void setScore() {
    }

    @Test
    public void incrementScore() {
    }

    @Test
    public void reset() {
    }
}