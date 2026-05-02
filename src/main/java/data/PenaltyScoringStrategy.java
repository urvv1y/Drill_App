package data;

import java.util.Map;
import java.util.Set;

public class PenaltyScoringStrategy implements ScoringStrategy {
    private Map<Integer, HowStupidAnswerPenalize> penalizeMap;

    public PenaltyScoringStrategy(Map<Integer, HowStupidAnswerPenalize> penalizeMap) {
        this.penalizeMap = penalizeMap;
    }

    @Override
    public int evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers) {
        int score = 0;

        for (Integer answer : userAnswers) {
            if (correctAnswers.contains(answer)) {
                score += 1;
            } else if (penalizeMap.containsKey(answer)) {
                HowStupidAnswerPenalize penalize = penalizeMap.get(answer);

                if (penalize == HowStupidAnswerPenalize.STUPID) {
                    score -= 1;
                } else if (penalize == HowStupidAnswerPenalize.VERY_STUPID) {
                    score -= 2;
                }
            }
        }
        return score;
    }
}
