package academy.utils;

/**
 * A generic record representing a pair of comparable values. Implements Comparable interface to allow natural ordering
 * of pairs. Comparison is primarily based on the first element, with the second element used as a tie-breaker.
 *
 * @param <F> the type of the first element, must implement Comparable
 * @param <S> the type of the second element, must implement Comparable
 */
public record Pair<F extends Comparable<F>, S extends Comparable<S>>(F first, S second)
        implements Comparable<Pair<F, S>> {
    @Override
    public int compareTo(Pair<F, S> pair) {
        return first.equals(pair.first) ? second.compareTo(pair.second) : first.compareTo(pair.first);
    }
}
