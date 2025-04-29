import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Autocomplete implements IAutocomplete {
    private Node root = new Node();
    private Database database = new Database();
    List<ITerm> suggestions;

    // add word with weight to trie
    @Override
    public void addWord(String word, long weight) {
        word = word.toLowerCase();

        // check for invalid characters
        for (char c : word.toCharArray()) {
            if (!Character.isAlphabetic(c)) {
                return;
            }
        }

        Term term = new Term(word, weight);
        Node current = this.root;
        current.setPrefixes(current.getPrefixes() + 1);

        // make nodes for each character
        for (int i = 0; i < word.length(); i++) {
            int index = word.charAt(i) - 'a';
            if (current.getReferences()[index] == null) {
                current.getReferences()[index] = new Node();
            }
            current = current.getReferences()[index];
            current.setPrefixes(current.getPrefixes() + 1);
        }

        // end of word reached
        current.setWords(current.getWords() + 1);
        current.setTerm(term);
    }

    // builds the trie
    @Override
    public Node buildTrie() {

            // parse each line for string and weight
            for (String title : database.getMovieNameSet()) {
                addWord(title, 0);
            }


        return this.root;
    }

    // gets root of subTrie
    @Override
    public Node getSubTrie(String prefix) {
        Node current = this.root;
        prefix = prefix.toLowerCase();

        // check for invalid characters
        for (char c : prefix.toCharArray()) {
            if (!Character.isAlphabetic(c)) {
                return null;
            }
        }

        for (int i = 0; i < prefix.length(); i++) {
            int index = prefix.charAt(i) - 'a';
            // prefix not in trie
            if (current.getReferences()[index] == null) {
                return null;
            }
            current = current.getReferences()[index];
        }
        return current;
    }

    // get number of words that start with prefix
    @Override
    public int countPrefixes(String prefix) {
        Node current = getSubTrie(prefix);
        if (current == null) {
            return 0;
        }
        return current.getPrefixes();
    }

    // get list of ITerm objects that start with prefix
    @Override
    public List<ITerm> getSuggestions(String prefix) {
        suggestions = new ArrayList<>();
        Node current = getSubTrie(prefix);
        // if no objects start with prefix
        if (current == null) {
            return suggestions;
        }

        getSuggestionsHelper(current);
        suggestions.sort(ITerm.byReverseWeightOrder());

        return suggestions;
    }

    // recursively get words from trie
    private void getSuggestionsHelper(Node current) {
        if (current == null) {
            return;
        }

        // check if current is a word then add to list
        if (current.getWords() == 1) {
            Term term = current.getTerm();
            Term copy = new Term(term.getTerm(), term.getWeight());
            suggestions.add(copy);
        }

        // traverse the trie
        for (int i = 0; i < 26; i++) {
            if (current.getReferences()[i] != null) {
                getSuggestionsHelper(current.getReferences()[i]);
            }
        }
    }
}
