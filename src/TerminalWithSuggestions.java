import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
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
    private int cursorPosition = 0;

    private int secondsRemaining = 30;
    private boolean timerRunning = true;
    private ScheduledExecutorService scheduler;
    private Database database = new Database();
    private String selectedTitle = "";
    private int suggestionIndex = 0;
    private int roundNumber = 0;

    private Deque<String> movieHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 5;

    private String player1Name = "";
    private String player2Name = "";
    private boolean gameStarted = false;
    private boolean enteringPlayer1 = true;
    private boolean shouldExit = false;

    public TerminalWithSuggestions() throws IOException {
        terminal = new DefaultTerminalFactory().createTerminal();
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
                    // Just wait for game over processing
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
                    case Character:
                        handleCharacter(Character.toLowerCase(keyStroke.getCharacter()));
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
        printColoredString(10, 2, "Welcome to Movie Battle!", TextColor.ANSI.MAGENTA_BRIGHT);

        TextColor player1Color = enteringPlayer1 ? TextColor.ANSI.BLUE_BRIGHT : TextColor.ANSI.WHITE;
        TextColor player2Color = !enteringPlayer1 ? TextColor.ANSI.BLUE_BRIGHT : TextColor.ANSI.WHITE;

        printColoredString(10, 4, "Enter Player 1 Name: " + player1Name, player1Color);
        printColoredString(10, 6, "Enter Player 2 Name: " + player2Name, player2Color);

        if (!enteringPlayer1 && !player1Name.isEmpty() && !player2Name.isEmpty()) {
            printColoredString(10, 8, "Press Enter to Start", TextColor.ANSI.GREEN_BRIGHT);
        }

        int cursorCol = 10 + (enteringPlayer1 ? "Enter Player 1 Name: ".length() + player1Name.length() : "Enter Player 2 Name: ".length() + player2Name.length());
        int cursorRow = enteringPlayer1 ? 4 : 6;
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
        } else {
            selectedTitle = currentInput.toString();
        }
        if (!selectedTitle.isEmpty()) {
            selectedTitle = capitalizeTitle(selectedTitle);
            movieHistory.addFirst(selectedTitle);
            if (movieHistory.size() > MAX_HISTORY_SIZE) {
                movieHistory.removeLast();
            }
        }

        secondsRemaining = 30;
        roundNumber++;
        currentInput = new StringBuilder();
        cursorPosition = 0;
        suggestions.clear();
    }

    private void updateSuggestions() {
        suggestions.clear();
        String prefix = currentInput.toString();
        if (!prefix.isEmpty()) {
            for (String word : dictionary) {
                if (word.startsWith(prefix.toLowerCase()) && suggestions.size() < 5) {
                    suggestions.add(word);
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

        String title = "Movie Battle";
        int titleCol = (size.getColumns() - title.length()) / 2;
        printColoredString(titleCol + 1, 0, title, TextColor.ANSI.MAGENTA_BRIGHT);

        int startRow = size.getRows() - MAX_HISTORY_SIZE - 6;
        int centerCol = size.getColumns() / 2;

        int k = 0;
        for (String t : movieHistory) {
            String titleCap = capitalizeTitle(t);
            int col = centerCol - titleCap.length() / 2;
            if (k > 0) {
                printColoredString(centerCol, startRow + 2 * k - 1, "|", TextColor.ANSI.GREEN_BRIGHT);
            }
            printString(col, startRow + 2 * k, titleCap);
            k++;
        }

        String timerText = secondsRemaining + "s";
        printString(titleCol + 5, 4, timerText);

        printString(0, 1, "Round: " + roundNumber);
        printString(0, 2, (roundNumber % 2 == 0 ? player1Name : player2Name) + "'s Turn");

        printColoredString(8, 4, player1Name, TextColor.ANSI.BLUE_BRIGHT);
        printColoredString(60, 4, player2Name, TextColor.ANSI.RED);

        printString(0, 6, "> " + currentInput.toString());

        int row = 7;
        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = capitalizeTitle(suggestions.get(i));
            if (i == suggestionIndex) {
                for (int j = 0; j < suggestion.length(); j++) {
                    screen.setCharacter(2 + j, row, new TextCharacter(
                            suggestion.charAt(j),
                            TextColor.ANSI.BLACK, TextColor.ANSI.WHITE));
                }
            } else {
                printString(2, row, suggestion);
            }
            row++;
        }

        screen.setCursorPosition(new TerminalPosition(cursorPosition + 2, 6));
        screen.refresh();
    }

    private void printString(int column, int row, String text) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(column + i, row,
                    new TextCharacter(text.charAt(i),
                            TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
        }
    }

    private void printColoredString(int column, int row, String text, TextColor color) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(column + i, row,
                    new TextCharacter(text.charAt(i),
                            color, TextColor.ANSI.BLACK));
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

        int terminalWidth = screen.getTerminalSize().getColumns();
        int terminalHeight = screen.getTerminalSize().getRows();

        String gameOverMessage = "Game Over!";
        int gameOverCol = (terminalWidth - gameOverMessage.length()) / 2;
        String winnerMessage = (roundNumber % 2 != 0 ? player1Name : player2Name) + " Wins!!";
        int winnerCol = (terminalWidth - winnerMessage.length()) / 2;
        printColoredString(winnerCol, terminalHeight / 2 + 1, winnerMessage, TextColor.ANSI.YELLOW_BRIGHT);
        printColoredString(gameOverCol, terminalHeight / 2, gameOverMessage, TextColor.ANSI.RED);

        String restartMessage = "Press R to play again or Q to quit.";
        int restartCol = (terminalWidth - restartMessage.length()) / 2;
        printColoredString(restartCol, terminalHeight / 2 + 2, restartMessage, TextColor.ANSI.WHITE);

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
        currentInput = new StringBuilder();
        suggestions.clear();
        roundNumber = 0;
        secondsRemaining = 30;
        gameStarted = false;
        enteringPlayer1 = true;
        player1Name = "";
        player2Name = "";

        screen.clear();
        showLandingScreen();
        screen.refresh();
    }

    private void exitGame() throws IOException {
        screen.clear();
        String exitMessage = "Thanks for playing! Exiting...";
        int exitCol = (screen.getTerminalSize().getColumns() - exitMessage.length()) / 2;
        printColoredString(exitCol, screen.getTerminalSize().getRows() / 2, exitMessage, TextColor.ANSI.CYAN);
        screen.refresh();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        shouldExit = true;
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