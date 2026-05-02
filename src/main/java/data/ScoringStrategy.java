package data;

import java.util.Set;

public interface ScoringStrategy {
    double evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers);
}
