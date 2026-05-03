package data;

import java.util.Set;

/**
 * Interface for scoring.
 * @author urvy.
 */
public interface ScoringStrategy {
    double evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers);
}
