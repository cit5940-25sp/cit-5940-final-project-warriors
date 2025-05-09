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

/**
 * The MovieGameView class represents the visual interface for the movie connection game.
 * It observes the MovieGameModel and updates its display based on model state changes.
 */
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
     * Initializes the terminal-based view for the Movie Battle game.
     */
    public MovieGameView(MovieGameModel model) throws IOException {
        // initialize model
        this.model = model;
        model.addObserver(this);

        // initialize terminal and screen
        terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(120, 40))
                .createTerminal();
        this.screen = new TerminalScreen(terminal);
        this.screen.startScreen();
    }

    /**
     * Polls and returns the next keyboard input from the user, if available.
     *
     * @return The next {@link KeyStroke} entered, or null if no input is available.
     * @throws IOException if polling the screen input fails.
     */
    public KeyStroke screenPollInput() throws IOException {
        return screen.pollInput();
    }

    /**
     * Cleans up the terminal and screen resources and removes the view as an observer from the model.
     *
     * @throws IOException if closing the terminal or screen fails.
     */
    public void cleanUp() throws IOException {
        if (model != null) {
            model.removeObserver(this);
        }
        screen.close();
        terminal.close();
    }

    /**
     * Resets the view by clearing the screen and showing the landing screen.
     *
     * @throws IOException if clearing or refreshing the screen fails.
     */
    public void resetView() throws IOException {
        screen.clear();
        showLandingScreen();
        screen.refresh();
    }

    /**
     * Displays the initial landing screen, including title, player name inputs, genre selection, and instructions.
     *
     * @throws IOException if screen drawing or refreshing fails.
     */
    public void showLandingScreen() throws IOException {
        screen.clear();
        screen.refresh();
        TerminalSize size = screen.getTerminalSize();

        drawBox(0, 0, size.getColumns() - 1, size.getRows() - 1);

        // Title and subtitle
        String title = "MOVIE BATTLE";
        int titleCol = (size.getColumns() - title.length()) / 2;
        printColoredString(titleCol, 2, title, TITLE_COLOR);

        String subtitle = "Connect movies and challenge your film knowledge!";
        int subtitleCol = (size.getColumns() - subtitle.length()) / 2;
        printColoredString(subtitleCol, 4, subtitle, TextColor.ANSI.WHITE);

        // Player name input boxes
        TextColor player1Color = model.getEnteringPlayer1() ? PLAYER1_COLOR : TextColor.ANSI.WHITE;
        TextColor player2Color = !model.getEnteringPlayer1() ? PLAYER2_COLOR : TextColor.ANSI.WHITE;

        drawBox((size.getColumns() / 2) - 25, 6, (size.getColumns() / 2) + 25, 8);
        drawBox((size.getColumns() / 2) - 25, 9, (size.getColumns() / 2) + 25, 11);

        printColoredString((size.getColumns() / 2) - 20, 7,
                "Player 1 Name: " + model.getPlayer1Name(), player1Color);
        printColoredString((size.getColumns() / 2) - 20, 10,
                "Player 2 Name: " + model.getPlayer2Name(), player2Color);

        // Genre selection section
        String[] genres = {
                "Action", "Adventure", "Animation", "Comedy",
                "Crime", "Drama", "Family", "Fantasy",
                "Horror", "Romance", "Sci-Fi", "Thriller"
        };
        String selectedGenre = model.getSelectedGenre(); // Assumes getter

        int genresPerRow = 4;
        int boxWidth = 18;
        int boxHeight = 3;
        int totalBoxWidth = genresPerRow * boxWidth;
        int startX = (size.getColumns() - totalBoxWidth) / 2;

        String genreHeader = "Select Win Condition Genre:";
        int genreHeaderCol = (size.getColumns() - genreHeader.length()) / 2;
        printColoredString(genreHeaderCol, 13, genreHeader, TextColor.ANSI.YELLOW);

        int genreBoxTop = 15;
        for (int i = 0; i < genres.length; i++) {
            int row = i / genresPerRow;
            int col = i % genresPerRow;

            int boxLeft = startX + col * boxWidth;
            int boxTop = genreBoxTop + row * boxHeight;
            int boxRight = boxLeft + boxWidth - 1;
            int boxBottom = boxTop + boxHeight - 1;

            String genre = genres[i];
            int textCol = boxLeft + (boxWidth - genre.length()) / 2;
            int textRow = boxTop + 1;

            boolean isSelected = model.isSelectingGenre() && i == model.getSelectedGenreIndex();
            TextColor genreColor = (model.isSelectingGenre() || !genre.equalsIgnoreCase(model.getSelectedGenre()))
                    ? TextColor.ANSI.WHITE
                    : TextColor.ANSI.GREEN_BRIGHT;

            if (isSelected) {
                drawColoredBox(boxLeft, boxTop, boxRight, boxBottom, TextColor.ANSI.YELLOW_BRIGHT);
            } else {
                drawBox(boxLeft, boxTop, boxRight, boxBottom);
            }

            printColoredString(textCol, textRow, genre, genreColor);
        }

        // Instructions section
        String[] instructions = {
                "How To Play:",
                "1. Players take turns naming movies",
                "2. Each movie must connect to the previous (shared actor, director, etc.)",
                "3. You have 30 seconds to make your move",
                "4. If time runs out, the other player wins",
                "5. Use arrow keys to navigate suggestions and Enter to select",
                "6. Power-ups: Press '[' to add 15 seconds to your time",
                "7. Power-ups: Press ']' to reduce opponent's time next turn"
        };

        int instructionStartRow = 25;
        int instructionCol = (size.getColumns() - 60) / 2;
        for (int i = 0; i < instructions.length; i++) {
            printColoredString(instructionCol, instructionStartRow + i, instructions[i], TextColor.ANSI.CYAN);
        }

        // Start prompt
        if (!model.getEnteringPlayer1() && !model.getPlayer1Name().isEmpty() && !model.getPlayer2Name().isEmpty()) {
            String startPrompt = "Press Enter to Start";
            int startCol = (size.getColumns() - startPrompt.length()) / 2;
            drawBox(startCol - 2, 34, startCol + startPrompt.length() + 2, 36);
            printColoredString(startCol, 35, startPrompt, TextColor.ANSI.GREEN_BRIGHT);
        }

        // Cursor positioning
        int cursorCol = model.getEnteringPlayer1() ?
                (size.getColumns() / 2) - 20 + "Player 1 Name: ".length() + model.getPlayer1Name().length() :
                (size.getColumns() / 2) - 20 + "Player 2 Name: ".length() + model.getPlayer2Name().length();
        int cursorRow = model.getEnteringPlayer1() ? 7 : 10;
        screen.setCursorPosition(new TerminalPosition(cursorCol, cursorRow));

        screen.refresh();
    }


    /**
     * Draws a colored box with borders at the specified coordinates.
     *
     * @param left   Left column position of the box.
     * @param top    Top row position of the box.
     * @param right  Right column position of the box.
     * @param bottom Bottom row position of the box.
     * @param color  The color used for the box's border.
     * @throws IOException if setting characters on the screen fails.
     */
    private void drawColoredBox(int left, int top, int right, int bottom, TextColor color) throws IOException {
        for (int col = left; col <= right; col++) {
            screen.setCharacter(col, top, new TextCharacter('─', color, TextColor.ANSI.DEFAULT));
            screen.setCharacter(col, bottom, new TextCharacter('─', color, TextColor.ANSI.DEFAULT));
        }
        for (int row = top; row <= bottom; row++) {
            screen.setCharacter(left, row, new TextCharacter('│', color, TextColor.ANSI.DEFAULT));
            screen.setCharacter(right, row, new TextCharacter('│', color, TextColor.ANSI.DEFAULT));
        }
        screen.setCharacter(left, top, new TextCharacter('┌', color, TextColor.ANSI.DEFAULT));
        screen.setCharacter(right, top, new TextCharacter('┐', color, TextColor.ANSI.DEFAULT));
        screen.setCharacter(left, bottom, new TextCharacter('└', color, TextColor.ANSI.DEFAULT));
        screen.setCharacter(right, bottom, new TextCharacter('┘', color, TextColor.ANSI.DEFAULT));
    }

    /**
     * Formats a set of genres into a comma-separated string.
     *
     * @param genres A set of genre names.
     * @return A comma-separated string representation of the genres.
     */
    private String formatGenreSet(Set<String> genres) {
        return String.join(", ", genres);
    }

    /**
     * Returns the current terminal size of the screen.
     *
     * @return The {@link TerminalSize} of the screen.
     */
    private TerminalSize getSize() {
        return screen.getTerminalSize();
    }

    /**
     * Updates the screen during gameplay, showing the current state including genre,
     * power-up information, scores, and current input.
     *
     * @param currentInput The current input string being entered by the user.
     * @throws IOException if screen clearing, drawing, or refreshing fails.
     */
    public void updateScreen(StringBuilder currentInput) throws IOException {
        if (model.getSecondsRemaining() == 0 && model.isGameStarted()) {
            return;
        }

        screen.clear();
        TerminalSize size = getSize();

        drawBox(0, 0, size.getColumns() - 1, size.getRows() - 1);

        // New header section with power-ups at top
        drawBox(0, 0, size.getColumns() - 1, 5);
        String title = "MOVIE BATTLE";
        int titleCol = (size.getColumns() - title.length()) / 2;
        printColoredString(titleCol, 1, title, TITLE_COLOR);

        // Show selected genre below the title
        if (model.getSelectedGenre() != null && !model.getSelectedGenre().isEmpty()) {
            String genreText = "Selected Genre: " + model.getSelectedGenre();
            int genreCol = (size.getColumns() - genreText.length()) / 2;
            printColoredString(genreCol, 2, genreText, GENRE_COLOR);
        }

        // Power-up display at top
        boolean isPlayer1Turn = (model.isPlayer1Turn());
        int timeBoostsRemaining = isPlayer1Turn ? model.getPlayer1TimeBoosts() : model.getPlayer2TimeBoosts();
        int timeSabotagesRemaining = isPlayer1Turn ? model.getPlayer1TimeSabotages() : model.getPlayer2TimeSabotages();

        String powerupInfo = "POWER-UPS: '[' Add Time (" + timeBoostsRemaining +
                " left) | ']' Sabotage Opponent (" + timeSabotagesRemaining + " left)";
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
        String turnInfo = (model.isPlayer1Turn() ? model.getPlayer1Name() : model.getPlayer2Name()) + "'s Turn";
        printColoredString(4, 10, roundInfo, TextColor.ANSI.WHITE);
        printColoredString(4, 11, turnInfo, (model.isPlayer1Turn() ? PLAYER1_COLOR : PLAYER2_COLOR));

        // Status effects display
        boolean nextPlayerWillBeSabotaged = false;
        if (model.isPlayer1Turn() && model.getPlayer1TimeSabotages() < 0) {
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

        if (!model.getLastFiveMovies().isEmpty()) {
            Iterator<Movie> movieIter = model.getLastFiveMovies().iterator();

            Movie firstMovie = movieIter.next();  // First movie (no connection yet)
            String titleCap = capitalizeTitle(firstMovie.getTitle());
            String firstGenre = formatGenreSet(firstMovie.getGenres());

            int col = centerCol - titleCap.length() / 2;
            printString(col, startRow, titleCap);

            String genreText = "[" + firstGenre + "]";
            int genreCol = centerCol - genreText.length() / 2;
            printColoredString(genreCol, startRow + 1, genreText, GENRE_COLOR);

            int k = 1;
            while (movieIter.hasNext()) {
                Movie currentMovie = movieIter.next();  // next movie
                Player currentPlayer = model.getLastFivePlayers().get(currentMovie.getTitle());
                Set<String> currentConnections = model.getLastFiveConnections().get(currentMovie.getTitle());

                if (currentConnections == null) {
                    System.out.println("No currentConnections found for: " + currentMovie.getTitle());
                    currentConnections = new HashSet<>();
                }

                for (int i = 0; i < 2; i++) {
                    printColoredString(centerCol, startRow + 2 + (k - 1) * 4 + i, "│", CONNECTION_COLOR);
                }

                String connection = "";
                for (String person : currentConnections) {
                    int hit = currentPlayer.getConnectionOfPerson(person);
                    connection += (person + "(" + hit + ")");
                }

                boolean isPlayer1Connection = currentPlayer.getUsername().equals(model.getPlayer1Name());
                TextColor connectionTextColor = isPlayer1Connection ? PLAYER1_COLOR : PLAYER2_COLOR;
                String arrowLeft = "← ";
                String arrowRight = " →";

                if (isPlayer1Connection) {
                    String connectionText = connection + arrowLeft;
                    int connectionLeftPos = centerCol - connectionText.length() - 1;
                    printColoredString(connectionLeftPos, startRow + 3 + (k - 1) * 4, connectionText, connectionTextColor);
                } else {
                    String connectionText = arrowRight + connection;
                    int connectionRightPos = centerCol + 1;
                    printColoredString(connectionRightPos, startRow + 3 + (k - 1) * 4, connectionText, connectionTextColor);
                }

                // Display current movie info
                String nextTitleCap = capitalizeTitle(currentMovie.getTitle());
                String nextGenreText = "[" + formatGenreSet(currentMovie.getGenres()) + "]";

                int nextCol = centerCol - nextTitleCap.length() / 2;
                printString(nextCol, startRow + 4 + (k - 1) * 4, nextTitleCap);

                int nextGenreCol = centerCol - nextGenreText.length() / 2;
                printColoredString(nextGenreCol, startRow + 5 + (k - 1) * 4, nextGenreText, GENRE_COLOR);

                // Prepare for next iteration
                k++;
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

    /**
     * Draws a rectangular box using Unicode characters on the screen.
     *
     * @param x1 The x-coordinate of the top-left corner.
     * @param y1 The y-coordinate of the top-left corner.
     * @param x2 The x-coordinate of the bottom-right corner.
     * @param y2 The y-coordinate of the bottom-right corner.
     */
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

    /**
     * Prints a white-colored string at the specified screen coordinates.
     *
     * @param column The starting column (x-coordinate).
     * @param row    The row (y-coordinate) where the text is printed.
     * @param text   The string to print.
     */
    private void printString(int column, int row, String text) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(column + i, row,
                    new TextCharacter(text.charAt(i),
                            TextColor.ANSI.WHITE, BACKGROUND_COLOR));
        }
    }

    /**
     * Prints a string with the specified foreground color at the given screen coordinates.
     *
     * @param column The starting column (x-coordinate).
     * @param row    The row (y-coordinate) where the text is printed.
     * @param text   The string to print.
     * @param color  The text color to use.
     */
    private void printColoredString(int column, int row, String text, TextColor color) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(column + i, row,
                    new TextCharacter(text.charAt(i),
                            color, BACKGROUND_COLOR));
        }
    }

    /**
     * Capitalizes the first letter of each word in the movie title.
     *
     * @param title The input movie title.
     * @return The capitalized movie title.
     */
    private String capitalizeTitle(String title) {
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

    /**
     * Displays the Game Over screen, announcing the winner and showing options to restart or quit.
     *
     * @param playerNum The number of the player who lost (used to determine the winner).
     * @throws IOException If an I/O error occurs during screen update.
     */
    public void showGameOverScreen(int playerNum) throws IOException {
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
        String winner = (playerNum == 1) ? model.getPlayer1Name() : model.getPlayer2Name();
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

    /**
     * Displays the exit screen with a goodbye message and closes the game view.
     *
     * @throws IOException If an I/O error occurs during screen update.
     */
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

    /**
     * Displays a power-up activation or failure message based on its type and activation status.
     *
     * @param powerUpType The type of power-up ("Time Boost" or "Time Sabotage").
     * @param activated   Whether the power-up was successfully activated.
     * @throws IOException If an I/O error occurs during screen update.
     */
    public void displayPowerUp(String powerUpType, boolean activated) throws IOException {
        String key = powerUpType + ":" + activated;
        switch (key) {
            case "Time Boost:true":
                showTemporaryMessage("Time Boost Activated: +15 seconds!", TextColor.ANSI.GREEN_BRIGHT, true);
                break;
            case "Time Boost:false":
                showTemporaryMessage("No Time Boosts Remaining!", TextColor.ANSI.RED, true);
                break;
            case "Time Sabotage:true":
                showTemporaryMessage("Time Sabotage Activated! Opponent's next turn will be shorter.", TextColor.ANSI.YELLOW, true);
                break;
            case "Time Sabotage:false":
                showTemporaryMessage("No Time Sabotages Remaining!", TextColor.ANSI.RED, true);
                break;
        }
    }

    /**
     * Displays a message indicating the user made an invalid guess.
     *
     * @throws IOException If an I/O error occurs during screen update.
     */
    public void displayInvalidGuess() throws IOException {
        showTemporaryMessage("Invalid guess! Try again.", TextColor.ANSI.RED, false);
    }

    /**
     * Shows a temporary, centered message inside a box for a brief duration.
     *
     * @param message      The message to display.
     * @param color        The color of the text.
     * @param restartTimer Whether to restart the screen session (e.g., for effects like time boosts).
     * @throws IOException If an I/O error occurs during screen update.
     */
    private void showTemporaryMessage(String message, TextColor color, boolean restartTimer) throws IOException {
        TerminalSize size = screen.getTerminalSize();
        int messageCol = (size.getColumns() - message.length()) / 2;

        if (restartTimer) {
            Screen tempScreen = new TerminalScreen(terminal);
            tempScreen.startScreen();
        }

        int boxWidth = message.length() + 4;
        int boxHeight = 3;
        int boxStartX = (size.getColumns() - boxWidth) / 2;
        int boxStartY = size.getRows() / 2 - 1;

        drawBox(boxStartX, boxStartY, boxStartX + boxWidth, boxStartY + boxHeight);
        printColoredString(messageCol, size.getRows() / 2, message, color);

        screen.refresh();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the current cursor position.
     *
     * @return The current cursor position.
     */
    public int getCursorPosition() {
        return this.cursorPosition;
    }

    /**
     * Sets the cursor position to the given value.
     *
     * @param pos The new cursor position.
     */
    public void setCursorPosition(int pos) {
        this.cursorPosition = pos;
    }

    /**
     * Increments the cursor position by 1.
     */
    public void incrementCursorPosition() {
        this.cursorPosition++;
    }

    /**
     * Decrements the cursor position by 1.
     */
    public void decrementCursorPosition() {
        this.cursorPosition--;
    }

    /**
     * Handles updates to the view based on a string-based event emitted by the model.
     *
     * @param event The type of event that occurred (e.g., "REFRESH", "GAME_OVER_1").
     */
    @Override
    public void update(String event) {
        System.out.println("View received event: " + event);
        try {
            switch (event) {
                case "REFRESH":
                    updateScreen(new StringBuilder());
                    break;
                case "GAME_OVER_1":
                    showGameOverScreen(1);
                    break;
                case "GAME_OVER_2":
                    showGameOverScreen(2);
                    break;
                case "GAME_START":
                    resetView();
                    break;
                default:
                    updateScreen(new StringBuilder());
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
