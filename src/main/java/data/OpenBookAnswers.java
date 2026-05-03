package data;

/**
 * Class for open style questions.
 * @author urvy.
 */
public class OpenBookAnswers extends Question {
    private String correctAnswer;

    public OpenBookAnswers(String subject,
                           Difficulty difficulty,
                           String text,
                           String correctAnswer
                          ) {
        super(subject, difficulty, text);
        this.correctAnswer = correctAnswer;
    }

    @Override
    public boolean isCorrect(Object answer) {
        if (answer instanceof String userAnswer) {
            return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        }
        return false;
    }

}
