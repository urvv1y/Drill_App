package data;

import java.util.Set;

public class CustomScoringStrategy implements ScoringStrategy {
    private int pointsTwoCorrect;
    private int pointsOneCorrectInTwo;
    private int pointsZeroCorrectInTwo;

    private int pointsOneCorrect;
    private int pointsOneWrong;

    public CustomScoringStrategy(int pointsTwoCorrect,
                                 int pointsOneCorrectInTwo,
                                 int pointsZeroCorrectInTwo,
                                 int pointsOneCorrect,
                                 int pointsOneWrong) {
        this.pointsTwoCorrect =pointsTwoCorrect;
        this.pointsOneCorrectInTwo =pointsOneCorrectInTwo;
        this.pointsZeroCorrectInTwo =pointsZeroCorrectInTwo;
        this.pointsOneCorrect =pointsOneCorrect;
        this.pointsOneWrong =pointsOneWrong;

    }




    @Override
    public int evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers) {

        int size = userAnswers.size();
        int correctCount = 0;

        for (Integer answer : userAnswers) {
            if (correctAnswers.contains(answer)) {
                correctCount++;
            }
        }

        if (size == 1) {
            return correctCount == 1 ? pointsOneCorrect : pointsOneWrong;
        }

        if (size == 2) {
            if (correctCount == 2) return pointsTwoCorrect;
            if (correctCount == 1) return pointsOneCorrectInTwo;
            return pointsZeroCorrectInTwo;
        }

        return 0;
    }
}

