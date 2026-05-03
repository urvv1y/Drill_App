package org.example;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppConfig implements Serializable {
    private Map<String, SubjectSettings> subjects = new HashMap<>();

    public void addSubject(String name, SubjectSettings subjectSettings) {
        subjects.put(name, subjectSettings);
    }

    public Map<String, SubjectSettings> getSubjects() {
        return subjects;
    }
}
