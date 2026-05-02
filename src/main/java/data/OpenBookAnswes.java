package data;

import java.util.Set;

public class OpenBookAnswes extends Question {
    private String correctAnswer;

    public OpenBookAnswes(String subject,
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
