import java.io.IOException;

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
