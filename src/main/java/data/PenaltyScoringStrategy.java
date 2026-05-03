package data;

import java.util.Map;
import java.util.Set;

/**
 * Strategy for penalizing, based on stupidity.
 * @author urvy.
 */
public class PenaltyScoringStrategy implements ScoringStrategy {
    private Map<Integer, HowStupidAnswerPenalize> penalizeMap;

    private int pointsOk;
    private int pointsStupid;
    private int pointsVeryStupid;

    public PenaltyScoringStrategy(Map<Integer, HowStupidAnswerPenalize> penalizeMap,
                                  int pointsOk, int pointsStupid, int pointsVeryStupid) {
        this.penalizeMap = penalizeMap;
        this.pointsOk = pointsOk;
        this.pointsStupid = pointsStupid;
        this.pointsVeryStupid = pointsVeryStupid;
    }

    @Override
    public double evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers) {
        double score = 0;
        for (Integer answer : userAnswers) {
            if (correctAnswers.contains(answer)) {
                score += pointsOk;
            } else if (penalizeMap.containsKey(answer)) {
                HowStupidAnswerPenalize penalize = penalizeMap.get(answer);
                if (penalize == HowStupidAnswerPenalize.STUPID) {
                    score += pointsStupid;
                } else if (penalize == HowStupidAnswerPenalize.VERY_STUPID) {
                    score += pointsVeryStupid;
                }
            }
        }
        return score;
    }
}