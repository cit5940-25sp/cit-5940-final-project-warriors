import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataBase {
    private Map<String, Set<String>> moviePeopleMap;

    public DataBase() {
        moviePeopleMap = new HashMap<>();
    }

    public void loadFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            // Read each line from the CSV
            while ((line = br.readLine()) != null) {
                // Skip the header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Split line by first comma (title, people)
                int firstCommaIndex = line.indexOf(',');
                if (firstCommaIndex == -1) continue;

                String title = line.substring(0, firstCommaIndex).trim();
                String peopleRaw = line.substring(firstCommaIndex + 1).trim();

                // Remove quotes from the people string
                if (peopleRaw.startsWith("\"") && peopleRaw.endsWith("\"")) {
                    peopleRaw = peopleRaw.substring(1, peopleRaw.length() - 1);
                }

                // Split people by comma and add them to a set
                String[] peopleArray = peopleRaw.split(",\\s*");
                Set<String> peopleSet = new HashSet<>(Arrays.asList(peopleArray));

                // Add the title and people set to the map
                moviePeopleMap.put(title, peopleSet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
