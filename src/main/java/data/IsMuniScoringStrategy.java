package data;

import java.util.Set;

/**
 * Scoring strategy, when there might be several correct answers and several bad answers with negative or partial points.
 * @author urvy
 */
public class IsMuniScoringStrategy implements ScoringStrategy {

    @Override
    public double evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers) {
        if (userAnswers.isEmpty()) {
            return 0.0;
        }
        if (correctAnswers.containsAll(userAnswers)) {
            if (userAnswers.size() == correctAnswers.size()) {
                return 1.0;
            }
            else {
                return (double) userAnswers.size() / correctAnswers.size();
            }
        }
        return 0.0;
    }
}