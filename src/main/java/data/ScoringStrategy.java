package data;

import java.util.Set;

public interface ScoringStrategy {
    int evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers);
}
