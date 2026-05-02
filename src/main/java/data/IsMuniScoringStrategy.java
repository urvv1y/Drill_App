package data;

import java.util.Set;

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