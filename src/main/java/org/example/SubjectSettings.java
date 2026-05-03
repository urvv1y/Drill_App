package org.example;

import data.Difficulty;

import java.io.Serializable;

public class SubjectSettings implements Serializable {
    private String filepath;
    private String quizType;
    private Difficulty difficulty;

    private int ptsTwoOk = 4;
    private int ptsOneOkInTwo = 0;
    private int ptsZeroOkInTwo = -2;
    private int ptsOneOk = 1;
    private int ptsOneWrong = -1;

    public SubjectSettings(String filepath, String quizType, Difficulty difficulty) {
        this.filepath = filepath;
        this.quizType = quizType;
        this.difficulty = difficulty;
    }

    public void setCustomScoring(int p2, int p1in2, int p0in2, int p1, int p1w) {
        this.ptsTwoOk = p2;
        this.ptsOneOkInTwo = p1in2;
        this.ptsZeroOkInTwo = p0in2;
        this.ptsOneOk = p1;
        this.ptsOneWrong = p1w;
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

    public int getPtsTwoOk() { return ptsTwoOk; }
    public int getPtsOneOkInTwo() { return ptsOneOkInTwo; }
    public int getPtsZeroOkInTwo() { return ptsZeroOkInTwo; }
    public int getPtsOneOk() { return ptsOneOk; }
    public int getPtsOneWrong() { return ptsOneWrong; }
}
