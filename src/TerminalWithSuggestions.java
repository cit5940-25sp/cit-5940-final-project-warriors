import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class TerminalWithSuggestions {
    private Terminal terminal;
    private Screen screen;
    private List<String> dictionary;
    private StringBuilder currentInput = new StringBuilder();
    private List<String> suggestions = new ArrayList<>();
    private List<String> suggestionGenres = new ArrayList<>();
    private int cursorPosition = 0;

    private int secondsRemaining = 30;
    private boolean timerRunning = true;
    private ScheduledExecutorService scheduler;
    private Database database = new Database();
    private String selectedTitle = "";
    private int suggestionIndex = 0;
    private int roundNumber = 0;

    // Power-up system
    private int player1TimeBoosts = 2;
    private int player2TimeBoosts = 2;
    private int player1TimeSabotages = 1;
    private int player2TimeSabotages = 1;

    private Deque<String> movieHistory = new ArrayDeque<>();
    private Deque<String> movieGenres = new ArrayDeque<>();
    private List<String> connections = new ArrayList<>();
    private List<Boolean> connectionOwners = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 5;

    private String player1Name = "";
    private String player2Name = "";
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean gameStarted = false;
    private boolean enteringPlayer1 = true;
    private boolean shouldExit = false;
    private boolean player1IsSabotaged = false;
    private boolean player2IsSabotaged = false;

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

    public TerminalWithSuggestions() throws IOException {
        terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(120, 40))
                .createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();

        database.loadFromCSV("cleaned_imdb_final.csv");
        List<String> titles = new ArrayList<>(database.getMovieNameSet());
        dictionary = new ArrayList<>();
        for (String movieName : titles) {
            movieName = movieName.toLowerCase();
            dictionary.add(movieName);
        }

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (timerRunning && secondsRemaining > 0 && gameStarted) {
                secondsRemaining--;
                try {
                    updateScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (secondsRemaining == 0 && gameStarted) {
                try {
                    gameOver();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void run() throws IOException {
        boolean running = true;
        screen.clear();
        screen.refresh();

        showLandingScreen();

        while (running && !shouldExit) {
            KeyStroke keyStroke = screen.pollInput();
            if (keyStroke != null) {
                if (!gameStarted) {
                    handleLandingInput(keyStroke);
                    continue;
                }

                if (secondsRemaining == 0) {
                    continue;
                }

                switch (keyStroke.getKeyType()) {
                    case ArrowUp:
                    if (!suggestions.isEmpty()) {
                        suggestionIndex = (suggestionIndex - 1 + suggestions.size()) % suggestions.size();
                    }
                    break;
                    case ArrowDown:
                        if (!suggestions.isEmpty()) {
                            suggestionIndex = (suggestionIndex + 1) % suggestions.size();
                        }
                        break;
                    case ArrowLeft:
                        if (!suggestions.isEmpty() && suggestionIndex >= 2) {
                            // Move from right column to left column
                            suggestionIndex = suggestionIndex % 2;
                        }
                        break;
                    case ArrowRight:
                        if (!suggestions.isEmpty() && suggestionIndex < 2) {
                            // Move from left column to right column
                            suggestionIndex = Math.min(suggestionIndex + 2, suggestions.size() - 1);
                        }
                        break;
                    case Character:
                        char c = keyStroke.getCharacter();
                        if (c == '1') {
                            activateTimeBoost();
                        } else if (c == '2') {
                            activateTimeSabotage();
                        } else {
                            handleCharacter(Character.toLowerCase(c));
                        }
                        break;
                    case Backspace:
                        handleBackspace();
                        break;
                    case Enter:
                        handleEnter();
                        break;
                    case EOF:
                    case Escape:
                        running = false;
                        break;
                    default:
                        break;
                }
                updateScreen();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        cleanup();
    }

    private void cleanup() throws IOException {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        screen.close();
        terminal.close();
    }

    private void handleLandingInput(KeyStroke keyStroke) throws IOException {
        if (keyStroke.getKeyType() == KeyType.Character) {
            if (enteringPlayer1) {
                player1Name += keyStroke.getCharacter();
            } else {
                player2Name += keyStroke.getCharacter();
            }
        } else if (keyStroke.getKeyType() == KeyType.Backspace) {
            if (enteringPlayer1 && player1Name.length() > 0) {
                player1Name = player1Name.substring(0, player1Name.length() - 1);
            } else if (!enteringPlayer1 && player2Name.length() > 0) {
                player2Name = player2Name.substring(0, player2Name.length() - 1);
            }
        } else if (keyStroke.getKeyType() == KeyType.Enter) {
            if (enteringPlayer1 && !player1Name.isEmpty()) {
                enteringPlayer1 = false;
            } else if (!enteringPlayer1 && !player2Name.isEmpty()) {
                gameStarted = true;
                currentInput = new StringBuilder();
                cursorPosition = 0;
                updateScreen();
            }
        }

        showLandingScreen();
    }

    private void showLandingScreen() throws IOException {
        screen.clear();
        TerminalSize size = screen.getTerminalSize();

        drawBox(0, 0, size.getColumns() - 1, size.getRows() - 1);

        String title = "🎬 MOVIE BATTLE 🎬";
        int titleCol = (size.getColumns() - title.length()) / 2;
        printColoredString(titleCol, 3, title, TITLE_COLOR);

        String subtitle = "Connect movies and challenge your film knowledge!";
        int subtitleCol = (size.getColumns() - subtitle.length()) / 2;
        printColoredString(subtitleCol, 5, subtitle, TextColor.ANSI.WHITE);

        TextColor player1Color = enteringPlayer1 ? PLAYER1_COLOR : TextColor.ANSI.WHITE;
        TextColor player2Color = !enteringPlayer1 ? PLAYER2_COLOR : TextColor.ANSI.WHITE;

        drawBox((size.getColumns() / 2) - 25, 10, (size.getColumns() / 2) + 25, 12);
        drawBox((size.getColumns() / 2) - 25, 15, (size.getColumns() / 2) + 25, 17);

        printColoredString((size.getColumns() / 2) - 20, 11, "Player 1 Name: " + player1Name, player1Color);
        printColoredString((size.getColumns() / 2) - 20, 16, "Player 2 Name: " + player2Name, player2Color);

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

        if (!enteringPlayer1 && !player1Name.isEmpty() && !player2Name.isEmpty()) {
            String startPrompt = "Press Enter to Start";
            int startCol = (size.getColumns() - startPrompt.length()) / 2;
            drawBox(startCol - 2, 30, startCol + startPrompt.length() + 2, 32);
            printColoredString(startCol, 31, startPrompt, TextColor.ANSI.GREEN_BRIGHT);
        }

        int cursorCol = enteringPlayer1 ?
                (size.getColumns() / 2) - 20 + "Player 1 Name: ".length() + player1Name.length() :
                (size.getColumns() / 2) - 20 + "Player 2 Name: ".length() + player2Name.length();
        int cursorRow = enteringPlayer1 ? 11 : 16;
        screen.setCursorPosition(new TerminalPosition(cursorCol, cursorRow));

        screen.refresh();
    }

    private void handleCharacter(char c) {
        currentInput.insert(cursorPosition, c);
        cursorPosition++;
        updateSuggestions();
    }

    private void handleBackspace() {
        if (cursorPosition > 0) {
            currentInput.deleteCharAt(cursorPosition - 1);
            cursorPosition--;
            updateSuggestions();
        }
    }

    private void handleEnter() throws IOException {
        if (!suggestions.isEmpty()) {
            selectedTitle = suggestions.get(suggestionIndex);
            String genre = suggestionGenres.get(suggestionIndex);
            movieGenres.addFirst(genre);
        } else {
            selectedTitle = currentInput.toString();
            String genre = "genre";
            movieGenres.addFirst(genre);
        }

        if (!selectedTitle.isEmpty()) {
            selectedTitle = capitalizeTitle(selectedTitle);
            movieHistory.addFirst(selectedTitle);

            if (movieHistory.size() > 1) {
                connections.add(0, generateConnectionPhrase());
                boolean isPlayer1 = (roundNumber % 2 == 0);
                connectionOwners.add(0, isPlayer1);

                if (isPlayer1) {
                    player1Score++;
                } else {
                    player2Score++;
                }
            }

            if (movieHistory.size() > MAX_HISTORY_SIZE) {
                movieHistory.removeLast();
                movieGenres.removeLast();
                if (connections.size() > MAX_HISTORY_SIZE - 1) {
                    connections.removeLast();
                    connectionOwners.removeLast();
                }
            }
        }

        // Check if the next player is sabotaged
        boolean nextPlayerIsSabotaged = false;
        if (roundNumber % 2 == 0) { // Player 1's turn just ended, checking if Player 2 is sabotaged
            if (player2IsSabotaged) {
                nextPlayerIsSabotaged = true;
                player2IsSabotaged = false; // Reset the flag
            }
        } else { // Player 2's turn just ended, checking if Player 1 is sabotaged
            if (player1IsSabotaged) {
                nextPlayerIsSabotaged = true;
                player1IsSabotaged = false; // Reset the flag
            }
        }

        secondsRemaining = nextPlayerIsSabotaged ? 20 : 30;
        roundNumber++;
        currentInput = new StringBuilder();
        cursorPosition = 0;
        suggestions.clear();
        suggestionGenres.clear();
    }

    // need to change to work for our actual connections
    private String generateConnectionPhrase() {
        return "Connection";
    }

    private void updateSuggestions() {
        suggestions.clear();
        suggestionGenres.clear();
        String prefix = currentInput.toString();
        if (!prefix.isEmpty()) {
            int count = 0;
            for (String word : dictionary) {
                if (word.startsWith(prefix.toLowerCase()) && count < 5) {
                    suggestions.add(word);
                    String genre = "genre";
                    suggestionGenres.add(genre);
                    count++;
                }
            }
        }
        suggestionIndex = 0;
    }

    private void updateScreen() throws IOException {
        if (secondsRemaining == 0 && gameStarted) {
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
        boolean isPlayer1Turn = (roundNumber % 2 == 0);
        int timeBoostsRemaining = isPlayer1Turn ? player1TimeBoosts : player2TimeBoosts;
        int timeSabotagesRemaining = isPlayer1Turn ? player1TimeSabotages : player2TimeSabotages;

        String powerupInfo = "POWER-UPS: [1] Add Time (" + timeBoostsRemaining +
                " left) | [2] Sabotage Opponent (" + timeSabotagesRemaining + " left)";
        printColoredString((size.getColumns() - powerupInfo.length()) / 2, 3, powerupInfo, TextColor.ANSI.YELLOW);

        // Player info below header
        drawBox(2, 6, size.getColumns() / 2 - 2, 8);
        drawBox(size.getColumns() / 2 + 1, 6, size.getColumns() - 3, 8);

        String player1Info = player1Name + " (Score: " + player1Score + ")";
        String player2Info = player2Name + " (Score: " + player2Score + ")";

        printColoredString(4, 7, player1Info, PLAYER1_COLOR);
        printColoredString(size.getColumns() / 2 + 3, 7, player2Info, PLAYER2_COLOR);

        // Current round and turn info
        String roundInfo = "Round: " + roundNumber;
        String turnInfo = (roundNumber % 2 == 0 ? player1Name : player2Name) + "'s Turn";
        printColoredString(4, 10, roundInfo, TextColor.ANSI.WHITE);
        printColoredString(4, 11, turnInfo, (roundNumber % 2 == 0 ? PLAYER1_COLOR : PLAYER2_COLOR));

        boolean nextPlayerWillBeSabotaged = false;
        if (roundNumber % 2 == 0) { // Player 1's turn now
            if (player2IsSabotaged) { // Player 2 will be sabotaged next
                nextPlayerWillBeSabotaged = true;
            }
        } else { // Player 2's turn now
            if (player1IsSabotaged) { // Player 1 will be sabotaged next
                nextPlayerWillBeSabotaged = true;
            }
        }

        if (nextPlayerWillBeSabotaged) {
            String effectText = "SABOTAGE ACTIVE: Reduced time next turn";
            printColoredString(4, 12, effectText, TextColor.ANSI.RED_BRIGHT);
        }

        // Timer with visual bar
        String timerText = "Time: " + secondsRemaining + "s";
        TextColor currentTimerColor = secondsRemaining > 20 ? TIMER_COLOR :
                (secondsRemaining > 10 ? TextColor.ANSI.YELLOW : TextColor.ANSI.RED_BRIGHT);
        int timerCol = size.getColumns() - timerText.length() - 5;
        printColoredString(timerCol, 10, timerText, currentTimerColor);


        int startRow = 14;
        int centerCol = size.getColumns() / 2;


        printColoredString(4, startRow, "Movie Chain:", TextColor.ANSI.WHITE);

        if (!movieHistory.isEmpty()) {
            Iterator<String> movieIter = movieHistory.iterator();
            Iterator<String> genreIter = movieGenres.iterator();

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
            while (movieIter.hasNext() && connIndex < connections.size()) {
                for (int i = 0; i < 2; i++) {
                    printColoredString(centerCol, startRow + 2 + (k-1)*4 + i, "│", CONNECTION_COLOR);
                }

                String connection = connections.get(connIndex);
                boolean isPlayer1Connection = connectionOwners.get(connIndex);
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
            if (i < suggestions.size()) {
                String suggestion = capitalizeTitle(suggestions.get(i));
                String displayText = suggestion;

                if (i == suggestionIndex) {
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
            if (i < suggestions.size()) {
                String suggestion = capitalizeTitle(suggestions.get(i));
                String displayText = suggestion;

                if (i == suggestionIndex) {
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

    private void gameOver() throws IOException {
        screen.clear();
        screen.setCursorPosition(new TerminalPosition(-1, -1));

        TerminalSize size = screen.getTerminalSize();
        int terminalWidth = size.getColumns();
        int terminalHeight = size.getRows();



        drawBox(terminalWidth / 4 + 2, terminalHeight / 4 + 2,
                terminalWidth * 3 / 4 - 2, terminalHeight * 3 / 4 - 2);

        String gameOverMessage = "GAME OVER";
        int gameOverCol = (terminalWidth - gameOverMessage.length()) / 2;

        boolean player1Wins = roundNumber % 2 != 0;
        String winner = player1Wins ? player1Name : player2Name;
        String winnerMessage = winner + " Wins!";
        int winnerCol = (terminalWidth - winnerMessage.length()) / 2;

        printColoredString(gameOverCol, terminalHeight / 3, gameOverMessage, TITLE_COLOR);
        printColoredString(winnerCol, terminalHeight / 3 + 2, winnerMessage,
                player1Wins ? PLAYER1_COLOR : PLAYER2_COLOR);

        String restartMessage = "Press R to play again or Q to quit";
        int restartCol = (terminalWidth - restartMessage.length()) / 2;
        printColoredString(restartCol, terminalHeight * 2 / 3, restartMessage, TextColor.ANSI.GREEN_BRIGHT);

        screen.refresh();

        boolean waitingForInput = true;
        while (waitingForInput) {
            KeyStroke keyStroke = screen.pollInput();
            if (keyStroke != null) {
                if (keyStroke.getKeyType() == KeyType.Character) {
                    char choice = Character.toUpperCase(keyStroke.getCharacter());
                    if (choice == 'R') {
                        restartGame();
                        waitingForInput = false;
                    } else if (choice == 'Q') {
                        exitGame();
                        waitingForInput = false;
                    }
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void restartGame() throws IOException {
        movieHistory.clear();
        movieGenres.clear();
        connections.clear();
        connectionOwners.clear();
        currentInput = new StringBuilder();
        suggestions.clear();
        suggestionGenres.clear();
        roundNumber = 0;
        secondsRemaining = 30;
        gameStarted = false;
        enteringPlayer1 = true;
        player1Name = "";
        player2Name = "";
        player1Score = 0;
        player2Score = 0;

        screen.clear();
        showLandingScreen();
        screen.refresh();
    }

    private void exitGame() throws IOException {
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

        shouldExit = true;
    }

    private void activateTimeBoost() throws IOException {
        boolean isPlayer1Turn = (roundNumber % 2 == 0);

        if ((isPlayer1Turn && player1TimeBoosts > 0) || (!isPlayer1Turn && player2TimeBoosts > 0)) {
            secondsRemaining += 15;

            if (isPlayer1Turn) {
                player1TimeBoosts--;
            } else {
                player2TimeBoosts--;
            }

            showPowerupEffect("Time Boost Activated: +15 seconds!", TextColor.ANSI.GREEN_BRIGHT);
        } else {
            showPowerupEffect("No Time Boosts Remaining!", TextColor.ANSI.RED);
        }
    }

    private void activateTimeSabotage() throws IOException {
        boolean isPlayer1Turn = (roundNumber % 2 == 0);

        if ((isPlayer1Turn && player1TimeSabotages > 0) || (!isPlayer1Turn && player2TimeSabotages > 0)) {
            if (isPlayer1Turn) {
                player1TimeSabotages--;
                player2IsSabotaged = true;
            } else {
                player2TimeSabotages--;
                player1IsSabotaged = true;
            }

            showPowerupEffect("Time Sabotage Activated! Opponent's next turn will be shorter.", TextColor.ANSI.YELLOW);
        } else {
            showPowerupEffect("No Time Sabotages Remaining!", TextColor.ANSI.RED);
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        updateScreen();
    }

    public static void main(String[] args) {
        try {
            TerminalWithSuggestions app = new TerminalWithSuggestions();
            app.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}