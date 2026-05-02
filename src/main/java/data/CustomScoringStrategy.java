package data;

import java.util.Set;

/**
 * Class for customizing the definitions of the questions.
 * Defines how many points will be added to the final score from selecting certain amount of answers.
 * e.g.:
 * pointsTwoCorrect = 4; You will get 4 points when selecting two correct answers;
 * pointsOneCorrectInTwo = 0; You will receive 0 points when selecting only one option from 2 needed;
 * pointsZeroCorrectInTwo = -2; You will receive -2 points when selecting 0 correct answers;
 * pointsOneCorrect = 1; You will receive 1 points from selecting one option;
 * pointsOneWrong = -1; You will receive -1 points from selecting wrong;
 * @author urvy.
 */
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
    public double evaluate(Set<Integer> userAnswers, Set<Integer> correctAnswers) {

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

