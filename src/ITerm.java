import java.util.Comparator;

/**
 * @author ericfouh
 */
public interface ITerm
        extends Comparable<ITerm> {

    /**
     * Compares the two terms in descending order by weight.
     *
     * @return comparator Object
     */
    public static Comparator<ITerm> byReverseWeightOrder() {
        return (o1, o2) -> (int) (o2.getWeight() - o1.getWeight());
    }


    /**
     * Compares the two terms in lexicographic order but using only the first r
     * characters of each query.
     *
     * @param r
     * @return comparator Object
     */
    public static Comparator<ITerm> byPrefixOrder(int r) {
        if (r < 0) {
            throw new IllegalArgumentException("illegal argument!");
        }
        return (o1, o2) -> {
            String s1 = o1.getTerm();
            String s2 = o2.getTerm();
            if (s1.length() > r) {
                s1 = s1.substring(0, r);
            }
            if (s2.length() > r) {
                s2 = s2.substring(0, r);
            }
            return s1.compareTo(s2);
        };

    }

    // Compares the two terms in lexicographic order by query.
    public int compareTo(ITerm that);


    // Returns a string representation of this term in the following format:
    // the weight, followed by a tab, followed by the query.
    public String toString();

    // Required getters.
    public long getWeight();

    public String getTerm();

    // Required setters (mostly for autograding purposes)
    public void setWeight(long weight);

    public void setTerm(String term);

}
