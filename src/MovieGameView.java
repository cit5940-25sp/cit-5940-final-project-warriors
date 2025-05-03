import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.*;

public class MovieGameView implements IObserver {
    private MovieGameModel model;

    private Screen screen;
    private Terminal terminal;
    private int cursorPosition = 0;

    // Colors
    private static final TextColor BACKGROUND_COLOR = TextColor.ANSI.BLACK;
    private static final TextColor TITLE_COLOR = TextColor.ANSI.MAGENTA_BRIGHT;
    private static final TextColor PLAYER1_COLOR = TextColor.ANSI.BLUE_BRIGHT;
    private static final TextColor PLAYER2_COLOR = TextColor.ANSI.RED;
    private static final TextColor TIMER_COLOR = TextColor.ANSI.YELLOW_BRIGHT;
    private static final TextColor CONNECTION_COLOR = TextColor.ANSI.GREEN_BRIGHT;
    private static final TextColor GENRE_COLOR = TextColor.ANSI.CYAN;
    private static final TextColor HIGHLIGHTED_TEXT = TextColor.ANSI.BLACK;
    private static final TextColor HIGHLIGHTED_BG = TextColor.ANSI.WHITE;

    /**
     * Initializes a new MovieGameView
     */
    public MovieGameView(MovieGameModel model) throws IOException {
        // initialize model
        this.model = model;

        // initialize terminal and screen
        terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(120, 40))
                .createTerminal();
        this.screen = new TerminalScreen(terminal);
        this.screen.startScreen();
    }


    public void clearScreen() {
        screen.clear();
    }

    public void screenRefresh() throws IOException {
        screen.refresh();
    }

    public KeyStroke screenPollInput() throws IOException {
        return screen.pollInput();
    }

    public void cleanUp() throws IOException {
        screen.close();
        terminal.close();
    }

    public void resetView() throws IOException {
        screen.clear();
        showLandingScreen();
        screen.refresh();
    }

    public void showLandingScreen() throws IOException {
        screen.clear();
        screen.refresh();
        TerminalSize size = screen.getTerminalSize();

        drawBox(0, 0, size.getColumns() - 1, size.getRows() - 1);

        String title = "🎬 MOVIE BATTLE 🎬";
        int titleCol = (size.getColumns() - title.length()) / 2;
        printColoredString(titleCol, 3, title, TITLE_COLOR);

        String subtitle = "Connect movies and challenge your film knowledge!";
        int subtitleCol = (size.getColumns() - subtitle.length()) / 2;
        printColoredString(subtitleCol, 5, subtitle, TextColor.ANSI.WHITE);

        TextColor player1Color = model.getEnteringPlayer1() ? PLAYER1_COLOR : TextColor.ANSI.WHITE;
        TextColor player2Color = !model.getEnteringPlayer1() ? PLAYER2_COLOR : TextColor.ANSI.WHITE;

        drawBox((size.getColumns() / 2) - 25, 10, (size.getColumns() / 2) + 25, 12);
        drawBox((size.getColumns() / 2) - 25, 15, (size.getColumns() / 2) + 25, 17);

        printColoredString((size.getColumns() / 2) - 20, 11,
                "Player 1 Name: " + model.getPlayer1Name(), player1Color);
        printColoredString((size.getColumns() / 2) - 20, 16,
                "Player 2 Name: " + model.getPlayer2Name(), player2Color);

        String[] instructions = {
                "How To Play:",
                "1. Players take turns naming movies",
                "2. Each movie must have a connection to the previous movie",
                "3. You have 30 seconds to make your move",
                "4. If time runs out, the other player wins",
                "5. Use arrow keys to navigate suggestions and Enter to select",
                "6. Power-ups: Press [1] to add 15 seconds to your time",
                "7. Power-ups: Press [2] to reduce opponent's time next turn"
        };

        int instructionCol = (size.getColumns() / 2) - 25;
        for (int i = 0; i < instructions.length; i++) {
            printColoredString(instructionCol, 22 + i, instructions[i], TextColor.ANSI.CYAN);
        }

        if (!model.getEnteringPlayer1() && !model.getPlayer1Name().isEmpty() && !model.getPlayer2Name().isEmpty()) {
            String startPrompt = "Press Enter to Start";
            int startCol = (size.getColumns() - startPrompt.length()) / 2;
            drawBox(startCol - 2, 30, startCol + startPrompt.length() + 2, 32);
            printColoredString(startCol, 31, startPrompt, TextColor.ANSI.GREEN_BRIGHT);
        }

        int cursorCol = model.getEnteringPlayer1() ?
                (size.getColumns() / 2) - 20 + "Player 1 Name: ".length() + model.getPlayer1Name().length() :
                (size.getColumns() / 2) - 20 + "Player 2 Name: ".length() + model.getPlayer2Name().length();
        int cursorRow = model.getEnteringPlayer1() ? 11 : 16;
        screen.setCursorPosition(new TerminalPosition(cursorCol, cursorRow));

        screen.refresh();
    }

    public void updateScreen(StringBuilder currentInput) throws IOException {
        if (model.getSecondsRemaining() == 0 && model.isGameStarted()) {
            return;
        }

        screen.clear();
        TerminalSize size = screen.getTerminalSize();

        drawBox(0, 0, size.getColumns() - 1, size.getRows() - 1);

        // New header section with power-ups at top
        drawBox(0, 0, size.getColumns() - 1, 5);
        String title = "MOVIE BATTLE";
        int titleCol = (size.getColumns() - title.length()) / 2;
        printColoredString(titleCol, 1, title, TITLE_COLOR);

        // Power-up display at top
        boolean isPlayer1Turn = (model.getRoundNumber() % 2 == 0);
        int timeBoostsRemaining = isPlayer1Turn ? model.getPlayer1TimeBoosts() : model.getPlayer2TimeBoosts();
        int timeSabotagesRemaining = isPlayer1Turn ? model.getPlayer1TimeSabotages() : model.getPlayer2TimeSabotages();

        String powerupInfo = "POWER-UPS: [1] Add Time (" + timeBoostsRemaining +
                " left) | [2] Sabotage Opponent (" + timeSabotagesRemaining + " left)";
        printColoredString((size.getColumns() - powerupInfo.length()) / 2, 3, powerupInfo, TextColor.ANSI.YELLOW);

        // Player info below header
        drawBox(2, 6, size.getColumns() / 2 - 2, 8);
        drawBox(size.getColumns() / 2 + 1, 6, size.getColumns() - 3, 8);

        String player1Info = model.getPlayer1Name() + " (Score: " + model.getPlayer1Score() + ")";
        String player2Info = model.getPlayer2Name() + " (Score: " + model.getPlayer2Score() + ")";

        printColoredString(4, 7, player1Info, PLAYER1_COLOR);
        printColoredString(size.getColumns() / 2 + 3, 7, player2Info, PLAYER2_COLOR);

        // Current round and turn info
        String roundInfo = "Round: " + model.getRoundNumber();
        String turnInfo = (model.getRoundNumber() % 2 == 0 ? model.getPlayer1Name() : model.getPlayer2Name()) + "'s Turn";
        printColoredString(4, 10, roundInfo, TextColor.ANSI.WHITE);
        printColoredString(4, 11, turnInfo, (model.getRoundNumber() % 2 == 0 ? PLAYER1_COLOR : PLAYER2_COLOR));

        // Status effects display
        boolean nextPlayerWillBeSabotaged = false;
        if (model.getRoundNumber() % 2 == 0 && model.getPlayer1TimeSabotages() < 0) {
            nextPlayerWillBeSabotaged = true;
        } else if (model.getRoundNumber() % 2 != 0 && model.getPlayer2TimeSabotages() < 0) {
            nextPlayerWillBeSabotaged = true;
        }

        if (nextPlayerWillBeSabotaged) {
            String effectText = "SABOTAGE ACTIVE: Reduced time next turn";
            printColoredString(4, 12, effectText, TextColor.ANSI.RED_BRIGHT);
        }

        // Timer with visual bar
        String timerText = "Time: " + model.getSecondsRemaining() + "s";
        TextColor currentTimerColor = model.getSecondsRemaining() > 20 ? TIMER_COLOR :
                (model.getSecondsRemaining() > 10 ? TextColor.ANSI.YELLOW : TextColor.ANSI.RED_BRIGHT);
        int timerCol = size.getColumns() - timerText.length() - 5;
        printColoredString(timerCol, 10, timerText, currentTimerColor);

        // Draw movie chain with connections and genres
        int startRow = 14;
        int centerCol = size.getColumns() / 2;

        //drawBox(2, startRow - 2, size.getColumns() - 3, size.getRows() - 7);
        printColoredString(4, startRow, "Movie Chain:", TextColor.ANSI.WHITE);

        if (!model.getMovieHistory().isEmpty()) {
            Iterator<String> movieIter = model.getMovieHistory().iterator();
            Iterator<String> genreIter = model.getMovieGenres().iterator();

            String firstMovie = movieIter.next();
            String firstGenre = genreIter.next();
            String titleCap = capitalizeTitle(firstMovie);

            int col = centerCol - titleCap.length() / 2;
            printString(col, startRow, titleCap);
            String genreText = "[" + firstGenre + "]";
            int genreCol = centerCol - genreText.length() / 2;
            printColoredString(genreCol, startRow + 1, genreText, GENRE_COLOR);

            int k = 1;
            int connIndex = 0;
            while (movieIter.hasNext() && connIndex < model.getConnections().size()) {
                for (int i = 0; i < 2; i++) {
                    printColoredString(centerCol, startRow + 2 + (k-1)*4 + i, "│", CONNECTION_COLOR);
                }

                String connection = model.getConnections().get(connIndex);
                boolean isPlayer1Connection = model.getConnectionOwners().get(connIndex);
                TextColor connectionTextColor = isPlayer1Connection ? PLAYER1_COLOR : PLAYER2_COLOR;
                String arrowLeft = "← ";
                String arrowRight = " →";

                if (isPlayer1Connection) {
                    String connectionText = connection + arrowLeft;
                    int connectionLeftPos = centerCol - connectionText.length() - 1;
                    printColoredString(connectionLeftPos, startRow + 3 + (k-1)*4, connectionText, connectionTextColor);
                } else {
                    String connectionText = arrowRight + connection;
                    int connectionRightPos = centerCol + 1;
                    printColoredString(connectionRightPos, startRow + 3 + (k-1)*4, connectionText, connectionTextColor);
                }

                String nextMovie = movieIter.next();
                String nextGenre = genreIter.next();
                String nextTitleCap = capitalizeTitle(nextMovie);
                int nextCol = centerCol - nextTitleCap.length() / 2;
                printString(nextCol, startRow + 4 + (k-1)*4, nextTitleCap);

                String nextGenreText = "[" + nextGenre + "]";
                int nextGenreCol = centerCol - nextGenreText.length() / 2;
                printColoredString(nextGenreCol, startRow + 5 + (k-1)*4, nextGenreText, GENRE_COLOR);

                k++;
                connIndex++;
            }
        }

        // Input field
        printString(4, size.getRows() - 6, "Select a Movie: ");
        String promptText = "> ";
        printString(4, size.getRows() - 5, promptText + currentInput.toString());


        // Draw suggestions with genres in two columns
        int leftColumnRow = size.getRows() - 4;
        int rightColumnRow = size.getRows() - 4;
        int rightColumnStart = size.getColumns() / 2 + 2;


        // Display first 2 suggestions in left column
        for (int i = 0; i < 2; i++) {
            if (i < model.getSuggestions().size()) {
                String suggestion = capitalizeTitle(model.getSuggestions().get(i));
                String displayText = suggestion;

                if (i == model.getSuggestionIndex()) {
                    for (int j = 0; j < displayText.length(); j++) {
                        screen.setCharacter(4 + j, leftColumnRow, new TextCharacter(
                                displayText.charAt(j),
                                HIGHLIGHTED_TEXT, HIGHLIGHTED_BG));
                    }
                } else {
                    printString(4, leftColumnRow, suggestion);
                }
            }
            leftColumnRow++;
        }

        // Display remaining 3 suggestions in right column
        for (int i = 2; i < 5; i++) {
            if (i < model.getSuggestions().size()) {
                String suggestion = capitalizeTitle(model.getSuggestions().get(i));
                String displayText = suggestion;

                if (i == model.getSuggestionIndex()) {
                    for (int j = 0; j < displayText.length(); j++) {
                        screen.setCharacter(rightColumnStart + j, rightColumnRow, new TextCharacter(
                                displayText.charAt(j),
                                HIGHLIGHTED_TEXT, HIGHLIGHTED_BG));
                    }
                } else {
                    printString(rightColumnStart, rightColumnRow, suggestion);
                }
            }
            rightColumnRow++;
        }

        screen.setCursorPosition(new TerminalPosition(cursorPosition + promptText.length() + 4, size.getRows() - 5));
        screen.refresh();
    }

    private void drawBox(int x1, int y1, int x2, int y2) {
        TextGraphics tg = screen.newTextGraphics();
        tg.setForegroundColor(TextColor.ANSI.WHITE);

        screen.setCharacter(x1, y1, new TextCharacter('┌'));
        screen.setCharacter(x2, y1, new TextCharacter('┐'));
        screen.setCharacter(x1, y2, new TextCharacter('└'));
        screen.setCharacter(x2, y2, new TextCharacter('┘'));

        for (int x = x1 + 1; x < x2; x++) {
            screen.setCharacter(x, y1, new TextCharacter('─'));
            screen.setCharacter(x, y2, new TextCharacter('─'));
        }

        for (int y = y1 + 1; y < y2; y++) {
            screen.setCharacter(x1, y, new TextCharacter('│'));
            screen.setCharacter(x2, y, new TextCharacter('│'));
        }
    }


    private void printString(int column, int row, String text) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(column + i, row,
                    new TextCharacter(text.charAt(i),
                            TextColor.ANSI.WHITE, BACKGROUND_COLOR));
        }
    }

    private void printColoredString(int column, int row, String text, TextColor color) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(column + i, row,
                    new TextCharacter(text.charAt(i),
                            color, BACKGROUND_COLOR));
        }
    }

    public String capitalizeTitle(String title) {
        String[] words = title.split(" ");
        StringBuilder capitalizedTitle = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedTitle.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase()).append(" ");
            }
        }
        return capitalizedTitle.toString().trim();
    }


    public void showGameOverScreen() throws IOException {
        screen.clear();
        screen.setCursorPosition(new TerminalPosition(-1, -1));

        TerminalSize size = screen.getTerminalSize();
        int terminalWidth = size.getColumns();
        int terminalHeight = size.getRows();



        drawBox(terminalWidth / 4 + 2, terminalHeight / 4 + 2,
                terminalWidth * 3 / 4 - 2, terminalHeight * 3 / 4 - 2);

        String gameOverMessage = "GAME OVER";
        int gameOverCol = (terminalWidth - gameOverMessage.length()) / 2;

        boolean player1Wins = model.getRoundNumber() % 2 != 0;
        String winner = player1Wins ? model.getPlayer1Name() : model.getPlayer2Name();
        String winnerMessage = winner + " Wins!";
        int winnerCol = (terminalWidth - winnerMessage.length()) / 2;

        printColoredString(gameOverCol, terminalHeight / 3, gameOverMessage, TITLE_COLOR);
        printColoredString(winnerCol, terminalHeight / 3 + 2, winnerMessage,
                player1Wins ? PLAYER1_COLOR : PLAYER2_COLOR);

        String restartMessage = "Press R to play again or Q to quit";
        int restartCol = (terminalWidth - restartMessage.length()) / 2;
        printColoredString(restartCol, terminalHeight * 2 / 3, restartMessage, TextColor.ANSI.GREEN_BRIGHT);

        screen.refresh();
    }

    public void showExitScreen() throws IOException {
        screen.clear();
        TerminalSize size = screen.getTerminalSize();

        String exitMessage = "Thanks for playing Movie Battle! Exiting...";
        int exitCol = (size.getColumns() - exitMessage.length()) / 2;

        drawBox(size.getColumns() / 4, size.getRows() / 3,
                size.getColumns() * 3 / 4, size.getRows() * 2 / 3);

        printColoredString(exitCol, size.getRows() / 2, exitMessage, TextColor.ANSI.CYAN);
        screen.refresh();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void displayPowerUp(String powerUpType, boolean activated) throws IOException {
        String key = powerUpType + ":" + activated;
        switch (key) {
            case "Time Boost:true":
                showPowerupEffect("Time Boost Activated: +15 seconds!", TextColor.ANSI.GREEN_BRIGHT);
                break;
            case "Time Boost:false":
                showPowerupEffect("No Time Boosts Remaining!", TextColor.ANSI.RED);
                break;
            case "Time Sabotage:true":
                showPowerupEffect("Time Sabotage Activated! Opponent's next turn will be shorter.", TextColor.ANSI.YELLOW);
                break;
            case "Time Sabotage:false":
                showPowerupEffect("No Time Sabotages Remaining!", TextColor.ANSI.RED);
                break;
        }
    }

    private void showPowerupEffect(String message, TextColor color) throws IOException {
        TerminalSize size = screen.getTerminalSize();
        int messageCol = (size.getColumns() - message.length()) / 2;

        Screen tempScreen = new TerminalScreen(terminal);
        tempScreen.startScreen();

        int boxWidth = message.length() + 4;
        int boxHeight = 3;
        int boxStartX = (size.getColumns() - boxWidth) / 2;
        int boxStartY = size.getRows() / 2 - 1;

        drawBox(boxStartX, boxStartY, boxStartX + boxWidth, boxStartY + boxHeight);
        printColoredString(messageCol, size.getRows() / 2, message, color);

        screen.refresh();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public void setCursorPosition(int pos) {
        this.cursorPosition = pos;
    }

    public void incrementCursorPosition() {
        this.cursorPosition++;
    }

    public void decrementCursorPosition() {
        this.cursorPosition--;
    }

    @Override
    public void update(String event) {

    }


//    /**
//     * Update the GameView based on the event that
//     * has occurred
//     */
//    @Override
//    public void update(String event) {
//
//        switch (event) {
//            case "GAME_START":
//                // get new display of game
//                break;
//            case "VALID_MOVE":
//                // update to reflect move made
//                break;
//            case "INVALID_MOVE":
//                // show error message
//                break;
//            case "GAME_END":
//                // show display of end screen
//                break;
//            default:
//                // update the entire view
//                break;
//        }
//    }

//    /**
//     * Gets the user's guess for a round of gameplay. Implements AutoComplete.
//     * @return String name of guess
//     */
//    public static String getUserGuess() {
//        return "";
//    }
}
