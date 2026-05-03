package org.example;

import data.Difficulty;

import java.io.Serializable;

public class SubjectSettings implements Serializable {
    private String filepath;
    private String quizType;
    private Difficulty difficulty;

    public SubjectSettings(String filepath, String quizType, Difficulty difficulty) {
        this.filepath = filepath;
        this.quizType = quizType;
        this.difficulty = difficulty;
    }

    public String getFilePath() {
        return filepath;
    }

    public String getQuizType() {
        return quizType;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public void setQuizType(String quizType) {
        this.quizType = quizType;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}
