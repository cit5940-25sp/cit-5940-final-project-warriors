import java.io.IOException;
/**
 * Main loop for running the Movie Game.
 */
public class MovieGame {
    public static void main(String[] args) {
        try {
            MovieGameController game = new MovieGameController();
            game.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
