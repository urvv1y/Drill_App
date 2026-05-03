package data;

import java.util.List;
import java.util.Set;

/**
 * Questions with multiplechoice.
 * May be used for penalization.
 * @author urvy.
 *
 */
public class MultipleChoiceQuestionWithOrWithoutPenalization extends Question {
    private List<String> options;
    private Set<Integer> correctOptionIndexes;
    private ScoringStrategy scoringStrategy;

    public MultipleChoiceQuestionWithOrWithoutPenalization(
            String subject,
            Difficulty difficulty,
            String text,
            List<String> options,
            Set<Integer> correctOptionIndexes,
            ScoringStrategy scoringStrategy) {

        super(subject, difficulty, text);
        this.options = options;
        this.correctOptionIndexes = correctOptionIndexes;
        this.scoringStrategy = scoringStrategy;
    }

    public double evaluateAnswer(Set<Integer> userAnswers) {
        return scoringStrategy.evaluate(userAnswers, correctOptionIndexes);
    }

    @Override
    public boolean isCorrect(Object answer) {
        if (answer instanceof Set<?> rawSet) {
            Set<Integer> userAnswers = (Set<Integer>) rawSet;
            return evaluateAnswer(userAnswers) > 0;
        }
        return false;
    }
}