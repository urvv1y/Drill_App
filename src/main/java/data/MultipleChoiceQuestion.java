package data;

import java.util.List;

public class MultipleChoiceQuestion extends Question {
    private List<String> correctAnswers;
    private int correctOptionIndex;

    public MultipleChoiceQuestion(String subject, Difficulty difficulty, String text, List<String> correctAnswers, int correctOptionIndex) {
        super(subject, difficulty, text);
        this.correctAnswers = correctAnswers;
        this.correctOptionIndex = correctOptionIndex;
    }

    @Override
    public boolean isCorrect(Object answer) {
        if (answer instanceof Integer userChoice) {
            return userChoice == correctOptionIndex;
        }
        return false;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }
}
