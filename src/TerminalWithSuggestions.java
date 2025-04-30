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
    Autocomplete autocomplete;

    // Timer variables
    private int                      secondsRemaining = 30;
    private boolean                  timerRunning = true;
    private ScheduledExecutorService scheduler;
    private Database database = new Database();
    private String selectedTitle = "";
    private int suggestionIndex = 0;
    private int roundNumber = 0;

    private Deque<String> movieHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 5;


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
        System.out.println(dictionary);


        // Initialize timer thread
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (timerRunning && secondsRemaining > 0) {
                secondsRemaining--;
                try {
                    updateScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void run() throws IOException {
        boolean running = true;

        screen.clear();

        cursorPosition = 2;
        updateScreen();

        while (running) {
            KeyStroke keyStroke = screen.pollInput();
            if (keyStroke != null) {
                switch (keyStroke.getKeyType()) {
                    case ArrowUp:
                        if (suggestionIndex > 0) {
                            suggestionIndex--;
                        }
                        break;
                    case ArrowDown:
                        if (suggestionIndex < suggestions.size() - 1) {
                            suggestionIndex++;
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

            // Small delay to prevent CPU hogging
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Shutdown timer
        scheduler.shutdown();
        screen.close();
        terminal.close();
    }

    private void handleCharacter(char c) {
        currentInput.insert(cursorPosition - 2, c);
        cursorPosition++;
        updateSuggestions();
    }

    private void handleBackspace() {
        if (cursorPosition > 2) {
            currentInput.deleteCharAt(cursorPosition - 3);
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
            movieHistory.addLast(selectedTitle);
            if (movieHistory.size() > MAX_HISTORY_SIZE) {
                movieHistory.removeFirst();
            }
        }

        int currentRow = screen.getCursorPosition().getRow();
        currentRow += 1 + suggestions.size();
        printString(0, currentRow, "> ");

        // add valid check logic using selectedTitle
        secondsRemaining = 30;

        roundNumber++;
        currentInput = new StringBuilder();
        cursorPosition = 2;
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
        synchronized (screen) {
            screen.clear();
            TerminalSize size = screen.getTerminalSize();

            // Print title centered at the top
            String title = "Movie Battle";
            int titleCol = (size.getColumns() - title.length()) / 2;
            printString(titleCol, 0, title);

            // Print movie history in bottom-right
            int startRow = size.getRows() - MAX_HISTORY_SIZE - 2;
            int startCol = Math.max(0, size.getColumns() - 40);
            printString(startCol, startRow - 1, "Recently Used");
            int k = 0;
            for (String t : movieHistory) {
                String display = (k + 1) + ". " + capitalizeTitle(t);
                printString(startCol, startRow + k, display);
                k++;
            }


            // Print timer at top right
            String timerText = secondsRemaining + "s";
            printString(titleCol + 5, 4, timerText);

            printString(0, 1, "Round: " + roundNumber);
            printString(0, 2, "Current Player: ");

            printColoredString(8, 4, "Player 1 ", TextColor.ANSI.BLUE_BRIGHT);
            printColoredString(60, 4, "Player 2 ", TextColor.ANSI.RED);
            // Print current command line
            printString(0, 6, "> " + currentInput.toString());

            // Print suggestions
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


            int selectedRow = size.getRows() - 10;
            printColoredString(0, selectedRow, "Selected: ", TextColor.ANSI.GREEN);
            printColoredString(10, selectedRow, selectedTitle, TextColor.ANSI.WHITE);

            screen.setCursorPosition(new TerminalPosition(cursorPosition, 6));
            screen.refresh();
        }
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



    public static void main(String[] args) {
        try {
            TerminalWithSuggestions app = new TerminalWithSuggestions();
            app.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}