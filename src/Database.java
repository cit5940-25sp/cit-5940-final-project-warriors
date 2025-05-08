import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Database {

     Map<String, Map<String, Set<String>>> moviePeopleMap;
     Set<String> movieNames;
     Map<String, Movie> movieMap;

    /**
     * Initialize a new MovieGameModel
     */
    public Database() {
        moviePeopleMap = new HashMap<>();
        movieNames = new HashSet<>();
        movieMap = new HashMap<>();
    }

    public void loadFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = parseCSVLine(line);

                String title = columns[0].trim();
                String peopleData = columns[1].trim();
                String genreData = columns[2].trim();
                String releaseDate = columns[3].trim();

                // Process people data
                Map<String, Set<String>> jobMap = new HashMap<>();
                Set<String> actors = new HashSet<>();
                Set<String> directors = new HashSet<>();
                Set<String> writers = new HashSet<>();
                Set<String> cinematographers = new HashSet<>();
                Set<String> composers = new HashSet<>();

                // Process entries
                List<String> peopleEntries = parsePeopleData(peopleData);

                for (String entry : peopleEntries) {
                    if (entry.contains(":")) {
                        String[] jobName = entry.split(":", 2);
                        String job = jobName[0].trim().toLowerCase();
                        String name = jobName[1].trim();
                        name = name.replaceAll("^\"|\"$", "").trim();

                        jobMap.computeIfAbsent(job, k -> new HashSet<>()).add(name);

                        switch (job) {
                            case "actor": actors.add(name); break;
                            case "director": directors.add(name); break;
                            case "writer": writers.add(name); break;
                            case "cinematographer": cinematographers.add(name); break;
                            case "composer": composers.add(name); break;
                        }
                    }
                }

                // Process genre data
                Set<String> genres = parseGenres(genreData);

                // Process release date
                int releaseYear = 0;
                if (!releaseDate.isEmpty()) {
                    try {
                        releaseYear = Integer.parseInt(releaseDate.substring(0, 4));
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        System.err.println("Failed to extract year for " + title + ": " + e.getMessage());
                    }
                }


                String titleWithYear = title + " (" + releaseYear + ")";

                moviePeopleMap.put(titleWithYear, jobMap);
                movieNames.add(titleWithYear);

                // Create movie object for Map
                Movie movie = new Movie(
                        titleWithYear,
                        releaseYear,
                        genres,
                        directors, actors, writers, cinematographers, composers
                );

                movieMap.put(titleWithYear, movie);
            }
        } catch (IOException e) {
            System.err.println("Failed to read CSV: " + e.getMessage());
        }
    }

    /**
     * Helper method to parse CSV line properly handling quoted fields
     */
    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }

        // Add the last token
        tokens.add(sb.toString());

        return tokens.toArray(new String[0]);
    }

    /**
     * Helper method to parse people data
     */
    private List<String> parsePeopleData(String peopleData) {
        List<String> entries = new ArrayList<>();

        // Remove any surrounding quotes if present
        if (peopleData.startsWith("\"") && peopleData.endsWith("\"")) {
            peopleData = peopleData.substring(1, peopleData.length() - 1);
        }

        String[] parts = peopleData.split(", (?=actor:|director:|writer:|cinematographer:|composer:)");

        for (String part : parts) {
            if (part.contains(":")) {
                entries.add(part.trim());
            }
        }

        return entries;
    }

    /**
     * Helper method to parse genre data
     */
    private Set<String> parseGenres(String genreData) {
        Set<String> genres = new HashSet<>();

        genreData = genreData.replaceAll("^\"|\"$", "");

        if (genreData.startsWith("{") && genreData.endsWith("}")) {
            genreData = genreData.substring(1, genreData.length() - 1);
        }

        String[] genreParts = genreData.split("',\\s*'");
        for (String part : genreParts) {
            part = part.replace("'", "").trim();
            if (part.startsWith("genres:")) {
                String genre = part.substring("genres:".length()).trim();
                genre = genre.replaceAll("^\"|\"$", "").trim();
                genres.add(genre);
            }
        }

        return genres;
    }

    /**
     * Return all people in the movie using movie title
     * @param title - String name of movie
     * @return allPeople
     */
    public Set<String> getPeopleByTitle(String title) {
        Map<String, Set<String>> roleMap = moviePeopleMap.get(title);
        if (roleMap == null) return Collections.emptySet();

        Set<String> allPeople = new HashSet<>();
        for (Set<String> names : roleMap.values()) {
            allPeople.addAll(names);
        }
        return allPeople;
    }

    /**
     * Generate a random movie to start the game with
     * @return Movie
     */
    public Movie getRandomMovie() {
//        Set<String> keySet = movieMap.keySet();
//        int randomIndex = new Random().nextInt(keySet.size());
//        String randomKey = (String) keySet.toArray()[randomIndex];
//        return movieMap.get(randomKey);
        return movieMap.get("Titanic (1997)");
    }

    /**
     * Get the set of all movie names. Used for implementing Autocomplete.
     * @return set of all movie names
     */
    public Set<String> getMovieNameSet() {
        return movieNames;
    }

    /**
     * Get the Movie class object by its unique name
     * @param name - String name of movie
     * @return Movie class object
     */
    public Movie getMovieByName(String name) {
        for (String key : movieMap.keySet()) {
            if (key.equalsIgnoreCase(name) || key.toLowerCase().startsWith(name.toLowerCase() + " (")) {
                return movieMap.get(key);
            }
        }
        System.err.println("Movie not found in database.");
        return null;
    }
}
