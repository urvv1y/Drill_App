package org.example;

import java.io.Serializable;

public class SubjectSettings implements Serializable {
    private String filepath;
    private String quizType;

    public SubjectSettings(String filepath, String quizType) {
        this.filepath = filepath;
        this.quizType = quizType;
    }

    public String getFilepath() {
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


}
