import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        assertEquals(1, player1.getConnections().size());
    }

    @Test
    public void getConnectionOfPerson() {
        Map<String, Integer> connections = new HashMap<>();
        connections.put("Leo DiCaprio", 3);
        player1.setConnections(connections);
        assertEquals(3, player1.getConnectionOfPerson("Leo DiCaprio"));
    }

    @Test
    public void updateConnections() {
        Map<String, Integer> connections = new HashMap<>();
        connections.put("Leo DiCaprio", 3);
        connections.put("Connection2", 2);
        player1.setConnections(connections);

        assertEquals(3, player1.getConnectionOfPerson("Leo DiCaprio"));
        assertEquals(2, player1.getConnectionOfPerson("Connection2"));
        Set<String> toUpdate = new HashSet<>();
        toUpdate.add("Connection2");
        player1.updateConnections(toUpdate);

        assertEquals(3, player1.getConnectionOfPerson("Leo DiCaprio"));
        assertEquals(3, player1.getConnectionOfPerson("Connection2"));
    }

    @Test
    public void testGetAndSetScore() {
        assertEquals(0, player1.getScore());
        player1.setScore(5000);
        assertEquals(5000, player1.getScore());
        for (int i = 0; i < 256; i ++) {
            player1.incrementScore();
        }
        assertEquals(5256, player1.getScore());
    }

    @Test
    public void reset() {
        Map<String, Integer> connections = new HashMap<>();
        connections.put("Leo DiCaprio", 3);
        player2.setConnections(connections);

        for (int i = 0; i < 256; i ++) {
            player2.incrementScore();
        }

        assertEquals("player2", player2.getUsername());
        assertEquals(256, player2.getScore());
        assertEquals(1, player2.getConnections().size());

        player2.reset();

        assertEquals("", player2.getUsername());
        assertEquals(0, player2.getScore());
        assertEquals(0, player2.getConnections().size());
    }
}