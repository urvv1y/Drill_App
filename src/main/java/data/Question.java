package data;

import java.util.List;

public abstract class Question {
    protected String subject;
    protected Difficulty difficulty;
    protected String text;

    public Question(String subject, Difficulty difficulty, String text) {
        this.subject = subject;
        this.difficulty = difficulty;
        this.text = text;
    }

    public abstract boolean isCorrect(Object answer);
    public String getText() {
        return text;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getSubject() {
        return subject;
    }
}